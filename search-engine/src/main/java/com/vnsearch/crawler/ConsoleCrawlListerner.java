package com.vnsearch.crawler;

import org.slf4.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleCrawlListerner implements CrawlListener {
    
    @Override
    public void onPageCrawled(CrawlEnvent e){

    }

    @Override

    public void onError(String url, Exception error){

    }
    @Override

    public void onDuplicatedContent(String url){

    }

    @Override
    public void onFinished(int totalPages, long elapsedMs){

    }
}
