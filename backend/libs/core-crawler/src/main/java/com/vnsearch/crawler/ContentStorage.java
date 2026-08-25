package com.vnsearch.crawler;

// Thư viện Jackson
// JavaObject --> JSON , JSON --> JavaObject
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vnsearch.model.WebDocument;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ContentStorage {

    private final ConcurrentHashMap<String, WebDocument> byUrl = new ConcurrentHashMap<>();

    public boolean save(WebDocument doc) {
        return byUrl.putIfAbsent(doc.getUrl(), doc) == null;
    }

    public boolean applyOutlinks(String url, List<String> outlinks) {
        if (url == null || outlinks == null) {
            return false;
        }
        WebDocument doc = byUrl.get(url);
        if (doc == null) {
            return false;
        }
        doc.setOutlinks(new ArrayList<>(outlinks));
        return true;
    }

    public int size() {
        return byUrl.size();
    }

    public List<WebDocument> all() {
        return new ArrayList<>(byUrl.values());
    }

    public static void saveToJson(List<WebDocument> documents, String path) throws IOException {
        Path filePath = Path.of(path);
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);

        Path temp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
        mapper.writeValue(temp.toFile(), documents);
        try {
            Files.move(temp, filePath, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static List<WebDocument> loadFromJson(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        WebDocument[] docs = mapper.readValue(new File(path), WebDocument[].class);
        return new ArrayList<>(List.of(docs));
    }
    
 }