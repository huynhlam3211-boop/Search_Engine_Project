package com.vnsearch.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vnsearch.config.AuditLogger;
import com.vnsearch.config.CallerIdentity;
import com.vnsearch.config.GlobalExceptionHandler;
import com.vnsearch.settings.SettingsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private static final int MAX_JSON_BYTES = 64 * 1024;

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final String EMPTY = "{}";

    private final SettingsRepository repository;
    private final AuditLogger audit;

    public SettingsController(SettingsRepository repository, AuditLogger audit) {
        this.repository = repository;
        this.audit = audit;
    }

    static class JsonKhongHopLe extends RuntimeException {
        JsonKhongHopLe(String message) {
            super(message);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> read() {
        String username = CallerIdentity.required();
        return repository.read(username)
                .map(SettingsController::okResponse)
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "settings", Map.of(),
                        "version", 0L)));
    }

    @PatchMapping
    public ResponseEntity<Map<String, Object>> merge(
            @RequestBody String body,
            @RequestHeader(value = "If-Match", required = false) Long expectedVersion) {
        return write(body, expectedVersion, true);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> replace(
            @RequestBody String body,
            @RequestHeader(value = "If-Match", required = false) Long expectedVersion) {
        return write(body, expectedVersion, false);
    }

    @DeleteMapping("/{khoa}")
    public ResponseEntity<Map<String, Object>> deleteKey(@PathVariable String khoa) {
        String username = CallerIdentity.required();
        return repository.deleteKey(username, khoa)
                .map(snapshot -> {
                    audit.record(username, "SETTINGS_DELETE_KEY", "user_settings:" + username,
                            "SUCCESS", "khoa=" + khoa);
                    return okResponse(snapshot);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping
    public ResponseEntity<Void> resetToDefaults() {
        String username = CallerIdentity.required();
        repository.deleteAll(username);
        audit.record(username, "SETTINGS_RESET", "user_settings:" + username, "SUCCESS", null);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Map<String, Object>> write(String body, Long expectedVersion,
                                                       boolean merge) {
        String username = CallerIdentity.required();
        String json = validate(body);

        Optional<SettingsRepository.Snapshot> result = merge
                ? repository.merge(username, json, expectedVersion)
                : repository.replace(username, json, expectedVersion);

        if (result.isEmpty()) {
            SettingsRepository.Snapshot current = repository.read(username)
                    .orElse(new SettingsRepository.Snapshot(EMPTY, 0L, Instant.now()));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "conflict",
                    "message", "Thiết bị khác đã sửa tuỳ chọn. Hãy gộp rồi thử lại.",
                    "settings", parseJson(current.json()),
                    "version", current.version()));
        }
        audit.record(username, merge ? "SETTINGS_MERGE" : "SETTINGS_REPLACE",
                "user_settings:" + username, "SUCCESS", null);
        return okResponse(result.get());
    }

    private static String validate(String body) {
        if (body == null || body.isBlank()) {
            throw new JsonKhongHopLe("Thân request rỗng.");
        }
        if (body.length() > MAX_JSON_BYTES) {
            throw new JsonKhongHopLe("Khối tuỳ chọn quá lớn (tối đa "
                    + (MAX_JSON_BYTES / 1024) + " KB).");
        }
        try {
            JsonNode node = JSON.readTree(body);
            if (!node.isObject()) {
                throw new JsonKhongHopLe("Tuỳ chọn phải là một đối tượng JSON.");
            }
            return node.toString();
        } catch (JsonProcessingException e) {
            throw new JsonKhongHopLe("JSON không hợp lệ: " + e.getOriginalMessage());
        }
    }

    private static ResponseEntity<Map<String, Object>> okResponse(
            SettingsRepository.Snapshot snapshot) {
        return ResponseEntity.ok()
                .eTag("\"" + snapshot.version() + "\"")
                .body(Map.of(
                        "settings", parseJson(snapshot.json()),
                        "version", snapshot.version(),
                        "updatedAt", snapshot.updatedAt()));
    }

    private static Object parseJson(String json) {
        try {
            return JSON.readTree(json == null ? EMPTY : json);
        } catch (JsonProcessingException e) {.
            throw new IllegalStateException("Cột settings chứa JSON hỏng", e);
        }
    }

    @ExceptionHandler(JsonKhongHopLe.class)
    public ResponseEntity<ProblemDetail> invalidJson(JsonKhongHopLe e) {
        return GlobalExceptionHandler.errorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }
}
