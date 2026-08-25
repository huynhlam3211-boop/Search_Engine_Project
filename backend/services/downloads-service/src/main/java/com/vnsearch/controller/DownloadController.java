package com.vnsearch.controller;

import com.vnsearch.config.AuditLogger;
import com.vnsearch.config.CallerIdentity;
import com.vnsearch.config.GlobalExceptionHandler;
import com.vnsearch.downloads.DownloadRecord;
import com.vnsearch.downloads.DownloadService;
import com.vnsearch.downloads.DownloadState;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/downloads")
public class DownloadController {

    private final DownloadService downloads;
    private final AuditLogger audit;

    public DownloadController(DownloadService downloads, AuditLogger audit) {
        this.downloads = downloads;
        this.audit = audit;
    }

    public record BatDauRequest(
            @NotBlank(message = "Thiếu id") String id,
            @NotBlank(message = "Thiếu địa chỉ nguồn") String sourceUrl,
            @NotBlank(message = "Thiếu tên tệp")
            @Size(max = 255, message = "Tên tệp quá dài") String fileName,
            @Size(max = 255) String mimeType,
            Long totalBytes,
            String localPath) {
    }

    public record CapNhatRequest(Long receivedBytes, DownloadState state, String localPath) {
    }

    @PostMapping
    public ResponseEntity<DownloadRecord.PublicView> start(
            @Valid @RequestBody BatDauRequest request,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {

        DownloadRecord record = downloads.start(CallerIdentity.required(),
                UUID.fromString(request.id()), request.sourceUrl(), request.fileName(),
                request.mimeType(), request.totalBytes(), request.localPath(), deviceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(record.toPublic(deviceId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DownloadRecord.PublicView> update(
            @PathVariable String id,
            @RequestBody CapNhatRequest request,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {

        return downloads.update(CallerIdentity.required(), UUID.fromString(id),
                        request.receivedBytes(), request.state(), request.localPath())
                .map(record -> ResponseEntity.ok(record.toPublic(deviceId)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<DownloadRecord.PublicView> list(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        return downloads.list(CallerIdentity.required(), page, size).stream()
                .map(record -> record.toPublic(deviceId))
                .toList();
    }

    @GetMapping("/active")
    public List<DownloadRecord.PublicView> listActive(
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        return downloads.listActive(CallerIdentity.required()).stream()
                .map(record -> record.toPublic(deviceId))
                .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        String username = CallerIdentity.required();
        boolean deleted = downloads.delete(username, UUID.fromString(id));
        if (deleted) {
            audit.record(username, "DOWNLOAD_DELETE", "downloads:" + id, "SUCCESS", null);
        }
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping
    public Map<String, Object> deleteFinished() {
        String username = CallerIdentity.required();
        int soDong = downloads.deleteFinished(username);
        audit.record(username, "DOWNLOAD_DELETE_ALL", null, "SUCCESS", "deleted=" + soDong);
        return Map.of("deleted", soDong);
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return Map.of("total", downloads.count(CallerIdentity.required()));
    }

    @ExceptionHandler(DownloadService.ChuyenTrangThaiKhongHopLe.class)
    public ResponseEntity<ProblemDetail> invalidTransition(
            DownloadService.ChuyenTrangThaiKhongHopLe e) {
        return GlobalExceptionHandler.errorResponse(HttpStatus.CONFLICT, e.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> invalidId(IllegalArgumentException e) {
        return GlobalExceptionHandler.errorResponse(HttpStatus.BAD_REQUEST,
                "Mã tải xuống không hợp lệ (phải là UUID).", null);
    }
}
