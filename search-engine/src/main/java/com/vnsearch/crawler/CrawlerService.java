package com.vnsearch.crawler;

import com.vnsearch.crawler.frontier.UrlFrontier;
import com.vnsearch.model.WebDocument;

public class CrawlerService {
    
    private final List<CrawlListener> listeners = new CopyOnWriteArrayList<>();
    public CrawlerService addListener(CrawlListener listener) {
        if (listener != null ) {
            listeners.add(listener);
        }
        return this;
    }

    private void processPage(CrawlTask task, CrawlConfig config){

    }

    private void notifyPageCrawled(CrawlListener.CrawlEvent event) {

    }

    private void notifyError(String url, Exception error) {
        
    }

    private void notifyDuplicateContent(String url) {
        
    }

    private void notifyFinished(int totalPages, long elapsedMs) {
        
    }
}
