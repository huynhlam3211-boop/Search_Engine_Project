package com.vnsearch.controller;

import com.vnsearch.crawler.bus.ImageFound;
import com.vnsearch.crawler.modular.ImageStore;
import com.vnsearch.model.SearchResponse;
import com.vnsearch.model.SearchResult;
import com.vnsearch.service.SearchEngineFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ImageSearchController { 
    
}