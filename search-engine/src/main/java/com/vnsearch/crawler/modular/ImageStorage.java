package com.vnsearch.crawler.modular;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vnsearch.crawler.bus.ImageFound;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class ImageStorage {

    private static final String SUFFIX = ".images.json";

    private ImageStorage() {

    }

    public static String pathFor(String corpusPath) {
        if (corpusPath == null || corpusPath.isBlank()) {
            throw new IllegalArgumentException("corpusPath không được rỗng");
        }
        String base = corpusPath.endsWith(".json")
                ? corpusPath.substring(0, corpusPath.length() - ".json".length())
                : corpusPath;
        return base + SUFFIX;
    }

    public static void saveToJson(Collection<ImageFound> images, String path) throws IOException {
        Path filePath = Path.of(path);
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        ObjectMapper mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);

        Path temp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
        mapper.writeValue(temp.toFile(), new ArrayList<>(images));
        try {
            Files.move(temp, filePath, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static List<ImageFound> loadFromJson(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ImageFound[] images = mapper.readValue(new File(path), ImageFound[].class);
        return new ArrayList<>(List.of(images));
    }

    public static List<ImageFound> loadQuietly(String path) {
        try {
            if (path == null || !Files.exists(Path.of(path))) {
                return List.of();
            }
            return loadFromJson(path);
        } catch (Exception e) {
            return List.of();
        }
    }
}