package com.vnsearch.crawler;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LinkExtractor {
    
    //** Trả về danh sách URL tuyệt đối , đã chuẩn hóa và khử trùng lặp */
    public List<String> extract(String baseUrl, Document document) {
        String canonicalBase = UrlCanonicalizer.canonicalize(baseUrl);
        Set<String> seen = new LinkedHashSet<>();

        Elements links = document.select("a[href]");
        for (Element link : links) {
            String absUrl = link.absUrl("href");
            if (absUrl == null || absUrl.isBlank()){
                continue;
            }

            if (!absUrl.startsWith("http://") && !absUrl.startsWith("https://")) {
                continue;
            }

            String canonical = UrlCanonicalizer.canonicalize(absUrl);
            if (!canonical.equals(canonicalBase)) {
                seen.add(canonical);
            }


        }

        return new ArrayList<>(seen);
    }
}