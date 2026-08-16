package com.vnsearch.crawle.modular;

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


public final class ImagesStorage {

    private static final String SUFFIX = ".image.json";

    private ImageStorage() {

    }

    public static String pathFor(String corpusPath) {

    }

    public static void saveToJson(Collectrion<ImageFound> images, String path) throws IOException {

    }

    public static List<ImageFound> loadFromJson(String path) throws IOException {

    }

    public static List<ImageFound> loadQuietly(String path) {
        
    }
}