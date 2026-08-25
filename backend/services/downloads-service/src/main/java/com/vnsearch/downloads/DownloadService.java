package com.vnsearch.downloads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DownloadService {

    private static final Logger log = LoggerFactory.getLogger(DownloadService.class);

    private static final int MAX_PAGE_SIZE = 200;

    private final DownloadRepository repository;
    private final Clock clock;

    public DownloadService(DownloadRepository repository) {
        this.repository = repository;
        this.clock = Clock.systemUTC();
    }

    public static class ChuyenTrangThaiKhongHopLe extends RuntimeException {
        public ChuyenTrangThaiKhongHopLe(DownloadState from, DownloadState to) {
            super("Không chuyển được từ " + from + " sang " + to + ".");
        }
    }

    @Transactional
    public DownloadRecord start(String username, UUID id, String sourceUrl, String fileName,
                                 String mimeType, Long totalBytes, String localPath,
                                 String deviceId) {
        Optional<DownloadRecord> daCo = repository.find(id, username);
        if (daCo.isPresent()) {
            return daCo.get();
        }
        DownloadRecord moi = new DownloadRecord(id, username, sourceUrl, fileName, mimeType,
                totalBytes, 0L, DownloadState.IN_PROGRESS, localPath, deviceId,
                clock.instant(), null, clock.instant());
        repository.save(moi);
        return moi;
    }

    @Transactional
    public Optional<DownloadRecord> update(String username, UUID id, Long receivedBytes,
                                            DownloadState newState, String localPath) {
        Optional<DownloadRecord> hienTai = repository.find(id, username);
        if (hienTai.isEmpty()) {
            return Optional.empty();
        }
        DownloadRecord cu = hienTai.get();
        DownloadState trangThai = newState == null ? cu.state() : newState;

        if (!cu.state().canTransitionTo(trangThai)) {
            throw new ChuyenTrangThaiKhongHopLe(cu.state(), trangThai);
        }

        long soByte = receivedBytes == null ? cu.receivedBytes()
                : Math.max(cu.receivedBytes(), receivedBytes);

        Instant ketThuc = trangThai.isTerminal()
                ? (cu.finishedAt() == null ? clock.instant() : cu.finishedAt())
                : null;

        DownloadRecord moi = new DownloadRecord(cu.id(), username, cu.sourceUrl(), cu.fileName(),
                cu.mimeType(), cu.totalBytes(), soByte, trangThai,
                localPath == null ? cu.localPath() : localPath,
                cu.deviceId(), cu.startedAt(), ketThuc, clock.instant());
        repository.save(moi);
        return Optional.of(moi);
    }

    public List<DownloadRecord> list(String username, int page, int size) {
        int limit = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return repository.findByUser(username, Math.max(page, 0) * limit, limit);
    }

    public List<DownloadRecord> listActive(String username) {
        return repository.findActive(username);
    }

    public boolean delete(String username, UUID id) {
        return repository.delete(id, username);
    }

    public int deleteFinished(String username) {
        int soDong = repository.deleteFinished(username);
        
        log.info("Xoá sổ tải xuống của {}: {} mục", username, soDong);
        return soDong;
    }

    public int count(String username) {
        return repository.count(username);
    }
}
