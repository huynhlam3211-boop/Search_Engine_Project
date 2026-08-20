package com.vnsearch.controller;

import com.vnsearch.crawler.bus.ImageFound;
import com.vnsearch.crawler.modular.ImageStore;
import com.vnsearch.model.WebDocument;
import com.vnsearch.service.SearchEngineFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class FeedController {
    
    private static final int MAX_SIZE = 50;
    private static final int DEFAULT_SIZE = 12;
    private static final int MAX_PAGE = 100;

    private static final int MAX_FEED_ITEMS = 200;
    private static final int SNIPPET_LENGTH = 160;

    private final SearchEngineFacade facade;
    private final ImageStore imageStore;

    private FeedController(SearchEngineFacade facade, ImageStore imageStore) {

    }

    @GetMapping("/feed")
    public Map<String, Object> feed() {
        
    }
}