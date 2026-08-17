package com.vnsearch.crawler.bus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

public record OutlinksExtracted(String sourceUrl, String host, List<String> outlinks,
                                 String jobId) { 
    
    public OutlinksExtracted {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new IllegalArgumentException("OutlinksExtracted.sourceUrl must not be empty");
        }
        outlinks = outlinks == null ? List.of() : List.copyOf(outlinks);
    }

    @JsonIgnore
    public int size() {
        return outlinks.size();
    }
}