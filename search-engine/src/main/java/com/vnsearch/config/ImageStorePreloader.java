package com.vnsearch.config;

import com.vnsearch.crawler.bus.ImageFound;
import com.vnsearch.crawler.modular.ImageStorage;
import com.vnsearch.crawler.modular.ImageStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ImageStorePreloader { 
    private static final Logger log = LoggerFactory.getLogger(ImageStorePreloader.class);

    @Value("${app.crawler.data-path}")

    private String crawledDataPath;

    private final ImageStore imageStore

    public ImageStorePreloader(ImageStore imageStore) {
        this.imageStore = imageStore;
    }

    @PostConstruct
    public void preload() { 
        
    }

}