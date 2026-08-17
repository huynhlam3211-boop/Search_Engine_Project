package com.vnsearch.crawler;

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

    }

    public boolean applyOutlinks(String url, List<String> outlinks) {

    }

    public int size() {

    }

    public List<WebDocument> all() {

    }

    public static void saveToJson(List<WebDocument> documents, String path) throws IOException {

    }

    public static List<WebDocument> loadFromJson(String path) throws IOException {
        
    }
    
 }