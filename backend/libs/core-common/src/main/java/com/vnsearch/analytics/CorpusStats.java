package com.vnsearch.analytics;

import com.vnsearch.datastructure.BloomFilter;
import com.vnsearch.datastructure.MinHeap;
import com.vnsearch.model.WebDocument;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public record CorpusStats(
        int documents,
        int distinctHosts,
        long totalOutlinks,
        int distinctLinkTargets,
        double avgOutlinks,
        int danglingDocuments,
        double avgDocLength,
        int medianDocLength,
        Instant oldestCrawledAt,
        Instant newestCrawledAt,
        List<UsageSnapshot.Counted> languages,
        List<UsageSnapshot.Counted> topHosts,
        List<DayCount> crawledPerDay) { 

}