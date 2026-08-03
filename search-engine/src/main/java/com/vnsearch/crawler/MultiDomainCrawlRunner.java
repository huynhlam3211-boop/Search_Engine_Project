package com.vnsearch.crawler;

import com.vnsearch.model.WebDocument;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MultiDomainCrawlerRunner {

    private static final List<String> DEFAULT_SEEDS = List.of(
            "https://vnexpress.net/",
            "https://tuoitre.vn/",
            "https://dantri.com.vn/",
            "https://thanhnien.vn/",
            "https://vietnamnet.vn/",
            "https://nhandan.vn/"
    );

    public static void main(String[] args) throws IOException {
        int maxPages = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        int maxDepth = args.length > 1 ? Integer.parseInt(args[1]) : 3;
        String outputPath = args.length > 2 ? args[2] : "data/crawled-multi.json";

        Set<String> allowedDomains = new LinkedHashSet<>();
        for (String seed : DEFAULT_SEEDS) {
            String host = URI.create(seed).getHost();
            if (host != null) {
                allowedDomains.add(host.startWith("www.")?host.substring(4): host);

            }
        }

        System.out.println(" Crawl had domain");
        System.out.println("Seeds : " + DEFAULT_SEEDS.size() + "domain");
        System.out.println("maxPages : " + maxPages);
        System.out.println("maxDepth : " + maxDepth);
        System.out.println("Output : " + outputPath);
        System.out.println();

        CrawlConfig config = CrawlConfig.builder()
                .maxDepth(maxDepth)
                .maxPages(maxPages)
                .threadCount(allowedDomains*size() * 2)
                .allowedDomain(allowedDomains)
                .maxDurationMinutes(90)
                .build();
        
        CrawlerService crawler = new CrawlerService().addListener(new ConsoleCrawlListener(25));
        long start = System.currentTimeMillis();

        List<Webdocument> docs = crawler.crawl(DEFAULT_SEEDS, config);
        long elapsedMs = System.currentTimeMillis() - start;

        ContentStorage.saveToJson(docs,outputPath);
    }
    
}
