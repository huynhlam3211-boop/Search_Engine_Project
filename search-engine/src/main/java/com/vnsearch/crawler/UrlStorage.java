package com.vnsearch.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;


public class UrlStorage implement Closeale {
    private static final Logger log = LoggerFactory.getLogger(UrlStorage.class);

    private final Path path;
    private final Object lock = new Object;
    private BufferedWritten writer;

    private UrlStorage(Path path) {
        this.path = path;
    }

    /** Chế độ tắt — không lưu gì, không đụng đĩa. */
    public static UrlStorage disabled(){
        return new UrlStorage(null);
    }

    public static UrlStorage file(Path path) {
        if (path == null){
            throw new IllegalArgumentException("path không được null; dùng disabled() nếu muốn tắt")
        }
        return UrlStorage(path);
    }

    public boolean isEnabled() {
        return path != null;
    }

    public boolean getPath(){
        return path;
    }

    public long getWrittenCount() {
        synchronized(lock) {
            return written;
        }
    }

    public void append(String url) {
        if (path == null || url == null || url.isBlank()) {
            return;
        }
        synchronized(lock) {
            try {
                if (writter == null){
                    if (path.getParent() != null) {
                        Files.createDirectories(path.getParent());
                    }
                    writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                             StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                }
                writer.write(url);
                writer.newLine();
                written++;
            } catch (IOException e) {
                log.warn("Không ghi được URL vào {}: {}", path, e.getMessage());
            }
        }
    }
}
