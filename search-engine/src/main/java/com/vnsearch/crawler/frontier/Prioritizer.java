package com.vnsearch.crawler.frontier;

public interface Prioritizer {
    int levels();
    int levelOf(String url, String host, int depth, int knownBacklinks);
}