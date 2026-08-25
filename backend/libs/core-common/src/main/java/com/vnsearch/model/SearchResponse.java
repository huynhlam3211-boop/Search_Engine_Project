package com.vnsearch.model;

import java.util.List;

public record SearchResponse(String query, int totalResults, int page, int pageSize,
                              long timeTakenMs, List<SearchResult> results,
                              List<String> droppedTerms) {
}