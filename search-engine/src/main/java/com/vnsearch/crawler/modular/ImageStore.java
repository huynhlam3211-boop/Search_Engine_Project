package com.vnsearch.crawler.modular;

import com.vnsearch.crawler.bus.ImageFound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ImageStore{

    public static final int MAX_PAGES = 50000;
    private final Map<String, ImageFound> byPage = new ConcurrentHashMap<>();

    private final AtomicLong pagesAdded = new AtomicLong();
    private final AtomicLong replaced = new AtomicLong();
    private final AtomicLong rejected = new AtomicLong();
    private final AtomicLong droppedPageLimit = new AtomicLong();

    public boolean add(ImageFound image) {
        if (image == null) {
            return false;
        }
        String pageUrl = image.pageUrl();
        if (!byPage.containsKey(pageUrl) && byPage.size() >= MAX_PAGES) {
            droppedPageLimit.incrementAndGet();
            return false;
        }

        boolean[] won = new boolean[1];
        byPage.compute(pageUrl, (url, current) -> {
            if (current == null) {
                won[0] = true;
                pagesAdded.incrementAndGet();
                return image;
            }

            if (ImageQuality.isBetter(image, current)) {
                won[0] = true;
                replaced.incrementAndGet();
                return image;
            }
            rejected.incrementAndGet();
            return current;
        });
        return won[0];
    }

    public List<ImageFound> forPage(String pageUrl) {
        ImageFound image = byPage.get(pageUrl);
        return image == null ? List.of() : List.of(image);
    }

    public List<ImageFound> forPages(List<String> pagesUrls, int limit) {
        List<ImageFound> out = new ArrayList<>();
        if (pageUrls == null || limit <=0) {
            return out;
        }
        Set<String> seen = new java.util.HashSet<>();
        for (String pagesUrl : pageUrls) {
            if (out.size() >= limit) {
                return out;
            }
            ImageFound image = byPage.get(pageUrl);
            if (image != null && seen.add(image.imageUrl())) {
                out.add(image);
            }
        }
        return out;
    }
    

    public List<ImageFound> all() {
        return new ArrayList<>(byPage.value());
    }

    public int addAll(Collection<ImageFound> images) {
        if (images == null) {
            return 0;
        }
        int changed = 0;
        for (ImageFound image: images) {
            if (add(image)) {
                changed++;
            }
        }
        return changed;
    }

    public int pageCount() {
        return byPage.size();
    }

    public long imageCoung() {
        return byPage.size();
    }

    public long getReplacedCount() {
        return replaced.get();
    }

    public long getRejectedCount() {
        return rejected.get();
    }

    public long getDroppedByPageLimitCount() {
        return droppedPageLimit.get();
    }

    public void clear() {
        byPage.clear();
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("pagesWithImages", byPage.size());
        stats.put("images", byPage.size());
        stats.put("candidatesRejected", rejected.get());
        stats.put("replaced", replaced.get());
        stats.put("pagesAdded", pagesAdded.get());
        stats.put("droppedPageLimit", droppedPageLimit.get());
        return stats;
    }
}