package com.vnsearch.model;

import java.time.Instant;

public record SearchResult(String title, String url, String snippet,
                            double score, double pageRankScore, Instant crawledAt) {
}