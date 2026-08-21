package com.vnsearch.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;


public class UrlStorage implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(UrlStorage.class);

    private final Path path;
    private final Object lock = new Object();
    private BufferedWriter writer;
    private long written;

    private UrlStorage(Path path) {
        this.path = path;
    }

    public static UrlStorage disabled(){
        return new UrlStorage(null);
    }

    public static UrlStorage file(Path path) {
        if (path == null){
            throw new IllegalArgumentException("path must not be null; use disabled() to turn storage off");
        }
        return new UrlStorage(path);
    }

    public boolean isEnabled() {
        return path != null;
    }

    public Path getPath() {
        return path;
    }

    public long getWrittenCount() {
        synchronized(lock) {
            return written;
        }
    }

    /**
     * Doc lai toan bo URL da ghi va day tung dong vao {@code consumer}.
     *
     * <p>Dung khi crawl noi tiep: nap lai Bloom filter tu phien truoc de khong
     * crawl lai nhung URL da thay.
     *
     * @return so dong da nap; 0 neu storage tat hoac tep chua ton tai
     */
    public long replay(Consumer<String> consumer) {
        if (path == null || consumer == null || !Files.exists(path)) {
            return 0L;
        }
        long count = 0L;
        synchronized (lock) {
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    consumer.accept(line);
                    count++;
                }
            } catch (IOException e) {
                log.warn("Failed to replay URLs from {}: {}", path, e.getMessage());
            }
        }
        return count;
    }

    public void append(String url) {
        if (path == null || url == null || url.isBlank()) {
            return;
        }
        synchronized(lock) {
            try {
                if (writer == null) {
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
                log.warn("Failed to write URL to {}: {}", path, e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            if (writer == null) {
                return;
            }
            try {
                writer.close();
            } catch (IOException e) {
                log.warn("Failed to close URL storage {}: {}", path, e.getMessage());
            } finally {
                writer = null;
            }
        }
    }
}
