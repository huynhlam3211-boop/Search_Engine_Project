package com.vnsearch.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RobotsTxtParser {
    
    record Rule(String path, boolean isAllow) {

    }

    private final Map<String, List<Rule>> cache = new ConcurrentHashMap<>();
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSecond(5))
        .build();
    
    public boolean isAllowed(String userAgent, String url) {
        try {
            URI uri = URI.create(url);
            String domainKey = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() >0) ? ":" + uri.getPort() : "");
            List<Rule> rules = cache.computeIfAbsent(domainKey, key -> fetchAndParse(key, userAgent));

            String path = uri.getRawPath()==null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
            return isPathAllowed(rules, path)
        } catch (Exception e) {
            return true;
        }
    }

    boolean isPathAllowed(List<Rule> rules, String path) {
        Rule best = null;
        for (Rule rule: rules) {

        }
    }

    List<Rule> parseForTest(String content, String userAgent) {
        List<Rule> rules = new ArrayList<>();
        parseInto(content, userAgent, rules);
        return rules
    }

    private List<Rule> fetchAndParse(String domainKey, String userAgent) {

    }

    private void parseInto() {

    }

    public static void main(String[] args) {
        RobotsTxtParser parser = new RobotsTxtParser();
        String ua = "VnSearchBot";
        String[] testUrls = {
            "https://vnexpress.net/",
                "https://vnexpress.net/tin-tuc/khoa-hoc",
                "https://vnexpress.net/wp-admin/"
        };
        for (String url : testUrls) {
            System.out.println("isAllowed(" + url + ") = " + parser.isAllowed(ua, url));
        }
    }

}