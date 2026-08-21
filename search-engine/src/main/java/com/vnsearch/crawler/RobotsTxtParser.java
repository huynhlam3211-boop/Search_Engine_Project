package com.vnsearch.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RobotsTxtParser {
    
    record Rule(String path, boolean isAllow) {

    }

    private final Map<String, List<Rule>> cache = new ConcurrentHashMap<>();
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
    
    public boolean isAllowed(String userAgent, String url) {
        try {
            URI uri = URI.create(url);
            String domainKey = uri.getScheme() + "://" + uri.getHost()
                    + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
            List<Rule> rules = cache.computeIfAbsent(domainKey, key -> fetchAndParse(key, userAgent));

            String path = uri.getRawPath()==null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
            return isPathAllowed(rules, path);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Quyet dinh mot duong dan co duoc phep khong theo luat "longest match wins".
     *
     * <p>Luat co tien to dai nhat khop voi {@code path} se thang. Neu mot Allow
     * va mot Disallow dai bang nhau thi Allow thang (theo chuan cua Google).
     * Khong luat nao khop thi mac dinh cho phep.
     */
    boolean isPathAllowed(List<Rule> rules, String path) {
        Rule best = null;
        for (Rule rule : rules) {
            if (!path.startsWith(rule.path())) {
                continue;
            }
            if (best == null
                    || rule.path().length() > best.path().length()
                    || (rule.path().length() == best.path().length() && rule.isAllow())) {
                best = rule;
            }
        }
        return best == null || best.isAllow();
    }

    List<Rule> parseForTest(String content, String userAgent) {
        List<Rule> rules = new ArrayList<>();
        parseInto(content, userAgent, rules);
        return rules;
    }

    private List<Rule> fetchAndParse(String domainKey, String userAgent) {
        List<Rule> rules = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(domainKey + "/robots.txt"))
                    .header("User-Agent", userAgent)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                parseInto(response.body(), userAgent, rules);
            }
            // Ma khac 200 (404, 5xx...) => khong co luat => cho phep tat ca
        } catch (IOException e) {
            // Khong lay duoc robots.txt thi cho phep, khong chan oan ca domain
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return rules;
    }

    /**
     * Doc noi dung robots.txt va do luat cua {@code userAgent} vao {@code rules}.
     *
     * <p>Neu tep co section rieng cho user-agent nay thi section do THAY THE
     * hoan toan section {@code *}; khong co thi dung section {@code *}.
     */
    private void parseInto(String content, String userAgent, List<Rule> rules) {
        if (content == null || content.isBlank()) {
            return;
        }
        String wanted = userAgent == null ? "" : userAgent.toLowerCase(Locale.ROOT);

        List<Rule> wildcardRules = new ArrayList<>();
        List<Rule> specificRules = new ArrayList<>();

        boolean inWildcard = false;
        boolean inSpecific = false;
        // Nhieu dong User-agent lien tiep dung chung mot khoi luat ben duoi
        boolean previousLineWasAgent = false;

        for (String rawLine : content.split("\\R")) {
            String line = rawLine;
            int hash = line.indexOf('#');
            if (hash >= 0) {
                line = line.substring(0, hash);
            }
            line = line.strip();
            if (line.isEmpty()) {
                continue;
            }

            int colon = line.indexOf(':');
            if (colon < 0) {
                continue;
            }
            String field = line.substring(0, colon).strip().toLowerCase(Locale.ROOT);
            String value = line.substring(colon + 1).strip();

            if ("user-agent".equals(field)) {
                if (!previousLineWasAgent) {
                    inWildcard = false;
                    inSpecific = false;
                }
                String agent = value.toLowerCase(Locale.ROOT);
                if ("*".equals(agent)) {
                    inWildcard = true;
                } else if (agent.equals(wanted)) {
                    inSpecific = true;
                }
                previousLineWasAgent = true;
                continue;
            }
            previousLineWasAgent = false;

            boolean isAllow = "allow".equals(field);
            if (!isAllow && !"disallow".equals(field)) {
                continue; // Sitemap, Crawl-delay... khong anh huong quyen truy cap
            }
            if (value.isEmpty()) {
                continue; // "Disallow:" rong nghia la khong chan gi
            }
            Rule rule = new Rule(value, isAllow);
            if (inSpecific) {
                specificRules.add(rule);
            }
            if (inWildcard) {
                wildcardRules.add(rule);
            }
        }

        rules.addAll(specificRules.isEmpty() ? wildcardRules : specificRules);
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