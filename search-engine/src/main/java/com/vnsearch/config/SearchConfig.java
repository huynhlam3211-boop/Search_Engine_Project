package com.vnsearch.config;

import com.vnsearch.index.Tokenizer;
import com.vnsearch.index.VietnameseTokenizer;
import com.vnsearch.crawler.modular.ImageStore;
import com.vnsearch.ranking.PageRankService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchConfig { 

    @Bean
    public Tokenizer tokenizer() {
        return new VietnameseTokenizer();
    }

    @Bean
    public PageRankService pageRankService() {
        return new PageRankService();
    }

    @Bean
    public ImageStore imageStore() {
        return new ImageStore();
    }
    
}