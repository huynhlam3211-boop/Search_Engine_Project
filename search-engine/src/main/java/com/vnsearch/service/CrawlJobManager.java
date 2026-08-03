package com.vnsearch.service;

import com.vnsearch.crawler.ConsoleCrawlListener;
import com.vnsearch.crawler.CrawlConfig;
import com.vnsearch.crawler.CrawlListener;
import com.vnsearch.crawler.CrawlerService;
import com.vnsearch.model.WebDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.utilconcurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Component
public class CrawlJobManager {

    private static final Logger log = LoggerFactory.getLogger(CrawlJobManager.class);
    private final Map<String, CrawlJob> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private static final class CrawlJob {

    }

    public String start(List<String> seedUrl, int maxDepth, int maxPages,
                        Consumer<List<WebDocument>> onSuccess) {
        
        String jobId = UUID.randomUUID().toString();
        CrawlerService crawler = new CrawlerService().addListener(new ConsoleCrawListener(25));
        CrawlJob job = new CrawlJob(crawler);
        jobs.put(jobId, job);

        executor.submit(() -> {
            try {
                job.trasitionTo(CrawlStatus.RUNNING);
                CrawlConfig config = CrawlConfig.builder() // Builder
                        .maxDepth(maxDepth)
                        .maxPages(maxPages)
                        .threadCount(4)
                        .allowedDomains(extractDomains(seedUrls))
                        .build(); 
                List<WebDocument> docs = crawler.crawl(seedUrls, config);
                onSuccess.accept(docs);
                job.transitionTo(CrawlStatus.DONE);
                
            } catch (Exception e) {
                log.error("Job crawl {} fail", jobId, e);
                job.errorMessage = e.getMessage();
                try {
                    job.transitionTo(CrawlStatus.FAILED);
                } catch (IllegalStateException ignored) {
                }

            }
        })

        return jobId;
        }
    )
    
}
