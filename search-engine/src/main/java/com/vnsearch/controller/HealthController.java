package com.vnsearch.controller;

import com.vnsearch.service.SearchEngineFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController { 
    private final SearchEngineFacade facade;

    public HealthController(SearchEngineFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String,Object>> health() {
        int documents = facade.getIndexedDocumentCount();
        boolean ready = documents > 0;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", ready ? "UP" : "OUT_OF_SERVICE");
        body.put("indexedDocuments", documents);

        return ready    
                ? ResponseEntity.ok(body)
                : ResponseEntity.status(503).body(body);
    }
}