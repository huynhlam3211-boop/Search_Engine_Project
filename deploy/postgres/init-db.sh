#!/bin/bash

set -euo pipefail

AUTH_PW="${AUTH_DB_PASSWORD:-$POSTGRES_PASSWORD}"
DOWNLOADS_PW="${DOWNLOADS_DB_PASSWORD:-$POSTGRES_PASSWORD}"
SETTINGS_PW="${SETTINGS_DB_PASSWORD:-$POSTGRES_PASSWORD}"

if [ "$AUTH_PW" = "$POSTGRES_PASSWORD" ]; then
  echo "CANH BAO: cac service dung chung mat khau voi superuser." >&2
  echo "          Dat AUTH_DB_PASSWORD / DOWNLOADS_DB_PASSWORD / SETTINGS_DB_PASSWORD" >&2
  echo "          truoc khi chay o moi truong that." >&2
fi

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-SQL
    -- ---------------------------------------------------------------
    -- auth-service — CSDL nhạy cảm nhất hệ thống.
    -- ---------------------------------------------------------------
    CREATE USER vnsearch_auth WITH PASSWORD '${AUTH_PW}';
    CREATE DATABASE vnsearch_auth OWNER vnsearch_auth;

    -- ---------------------------------------------------------------
    -- downloads-service
    -- ---------------------------------------------------------------
    CREATE USER vnsearch_downloads WITH PASSWORD '${DOWNLOADS_PW}';
    CREATE DATABASE vnsearch_downloads OWNER vnsearch_downloads;

    -- ---------------------------------------------------------------
    -- settings-service
    -- ---------------------------------------------------------------
    CREATE USER vnsearch_settings WITH PASSWORD '${SETTINGS_PW}';
    CREATE DATABASE vnsearch_settings OWNER vnsearch_settings;
SQL

for db in vnsearch_auth vnsearch_downloads vnsearch_settings; do
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$db" <<-SQL
      REVOKE ALL ON SCHEMA public FROM PUBLIC;
      GRANT ALL ON SCHEMA public TO ${db};
SQL
done

echo "Da tao 3 CSDL rieng: vnsearch_auth, vnsearch_downloads, vnsearch_settings"
