package com.vnsearch.downloads;

import java.time.Instant;
import java.util.UUID;

public record DownloadRecord(
        UUID id,
        String username,
        String sourceUrl,
        String fileName,
        String mimeType,
        Long totalBytes,
        long receivedBytes,
        DownloadState state,
        String localPath,
        String deviceId,
        Instant startedAt,
        Instant finishedAt,
        Instant updatedAt) {

    public record PublicView(
            UUID id,
            String sourceUrl,
            String fileName,
            String mimeType,
            Long totalBytes,
            long receivedBytes,
            Integer percent,
            DownloadState state,
            boolean onThisDevice,
            Instant startedAt,
            Instant finishedAt) {
    }

    public PublicView toPublic(String requestingDeviceId) {
        return new PublicView(id, sourceUrl, fileName, mimeType, totalBytes, receivedBytes,
                percent(), state,
                deviceId != null && deviceId.equals(requestingDeviceId),
                startedAt, finishedAt);
    }

    private Integer percent() {
        if (totalBytes == null || totalBytes <= 0) {
            return null;
        }
        return (int) Math.min(100, receivedBytes * 100 / totalBytes);
    }
}
