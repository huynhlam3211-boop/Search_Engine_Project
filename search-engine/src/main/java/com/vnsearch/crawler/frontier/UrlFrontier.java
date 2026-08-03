package com.vnsearch.crawler.frontier;

import com.vnsearch.crawler.UrlCanonicalizer;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UrlFrontier {
    public boolean addUrl(String rawUrl, int depth, int knownBacklinks) {
        String url = UrlCanonicalizer.canonicalizer(rawUrl);
        if (url == null || url.isBlank()) {
            return false;
        }

        String host = hostOf(url);
        CrawlTask task = new CrawlTask(url, host != null ? host : url, depth);
        int level = prioritizer.levelOf(url, task.host(), depth, knowBacklinks);

        synchronized(lock) {
            if (enqueued.contains(url)) {
                return false;
            }
            if (totalSize >= maxSize) {
                droppedDueToCapacity++;
                return false;
            }
            frontQueues.add(task, level);
            enqueued.add(url);
            pendingPerHost.merge(task.host(),1,Integer::sum);
            totalSize++;
            return true;
        }
    }
}
