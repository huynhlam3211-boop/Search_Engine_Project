package com.vnsearch.settings;

import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

@Repository
public class SettingsRepository {

    private final JdbcClient jdbc;

    public SettingsRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public record Snapshot(String json, long version, Instant updatedAt) {
    }

    public Optional<Snapshot> read(String username) {
        return jdbc.sql("""
                        SELECT settings::text AS settings, version, updated_at
                          FROM user_settings
                         WHERE username = :username
                        """)
                .param("username", username)
                .query((rs, rowNum) -> new Snapshot(
                        rs.getString("settings"),
                        rs.getLong("version"),
                        rs.getTimestamp("updated_at").toInstant()))
                .optional();
    }

    @Transactional
    public Optional<Snapshot> merge(String username, String newJson, Long expectedVersion) {
        int rows = jdbc.sql("""
                        INSERT INTO user_settings (username, settings, version)
                        VALUES (:username, :settings::jsonb, 1)
                        ON CONFLICT (username) DO UPDATE SET
                               settings   = user_settings.settings || EXCLUDED.settings,
                               version    = user_settings.version + 1,
                               updated_at = now()
                         WHERE :expectedVersion::bigint IS NULL
                            OR user_settings.version = :expectedVersion::bigint
                        """)
                .param("username", username)
                .param("settings", newJson)
                .param("expectedVersion", expectedVersion)
                .update();

        return rows == 0 ? Optional.empty() : read(username);
    }

    @Transactional
    public Optional<Snapshot> replace(String username, String newJson, Long expectedVersion) {
        int rows = jdbc.sql("""
                        INSERT INTO user_settings (username, settings, version)
                        VALUES (:username, :settings::jsonb, 1)
                        ON CONFLICT (username) DO UPDATE SET
                               settings   = EXCLUDED.settings,
                               version    = user_settings.version + 1,
                               updated_at = now()
                         WHERE :expectedVersion::bigint IS NULL
                            OR user_settings.version = :expectedVersion::bigint
                        """)
                .param("username", username)
                .param("settings", newJson)
                .param("expectedVersion", expectedVersion)
                .update();
        return rows == 0 ? Optional.empty() : read(username);
    }

    @Transactional
    public Optional<Snapshot> deleteKey(String username, String khoa) {
        jdbc.sql("""
                        UPDATE user_settings
                           SET settings   = settings - :khoa,
                               version    = version + 1,
                               updated_at = now()
                         WHERE username = :username
                        """)
                .param("username", username)
                .param("khoa", khoa)
                .update();
        return read(username);
    }

    @Transactional
    public void deleteAll(String username) {
        jdbc.sql("DELETE FROM user_settings WHERE username = :username")
                .param("username", username)
                .update();
    }

    static PGobject jsonb(String json) throws SQLException {
        PGobject object = new PGobject();
        object.setType("jsonb");
        object.setValue(json);
        return object;
    }
}
