package com.vnsearch.analytics;

import java.util.List; 

public record UsageSnapshot(
        long visitors,
        long signedInVisitors,
        long activeVisitors,
        int activeWindowMinutes,
        long searches,
        long clicks,
        double clickThroughRate,
        double avgLatencyMs,
        long zeroResultSearches,
        double zeroResultRate,
        double avgSessionMinutes,
        List<HourPoint> hourly,
        List<LatencyBucket> latency,
        List<Counted> topQueries,
        List<LinkCount> topLinks,
        List<Counted> topHosts,
        List<Counted> topUsers,
        boolean truncated) { 

}