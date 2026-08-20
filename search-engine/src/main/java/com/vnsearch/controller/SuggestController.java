package com.vnsearch.controller;

import com.vnsearch.service.SearchEngineFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SuggestController { 

    private static final int MAX_PAGE = 1_000;
    private static final int MAX_SIZE = 100;
    private static final int DEFAULT_SIZE = 20;

    private final SearchEngineFacade facade;

    public SearchController(SearchEngineFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/search")
    public SearchResponse search(@RequestParam("q") String q,
                                  @RequestParam(value = "page", defaultValue = "1") int page,
                                  @RequestParam(value = "size", defaultValue = "20") int size) {
        int safePage = Math.min(Math.max(page, 1), MAX_PAGE);
        int safeSize = size < 1 || size > MAX_SIZE ? DEFAULT_SIZE : size;
        return facade.search(q, safePage, safeSize);
    }
}