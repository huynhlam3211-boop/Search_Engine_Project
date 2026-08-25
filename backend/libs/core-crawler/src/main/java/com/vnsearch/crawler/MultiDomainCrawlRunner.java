package com.vnsearch.crawler;

import com.vnsearch.crawler.bus.ImageFound;
import com.vnsearch.crawler.modular.ImageStorage;
import com.vnsearch.crawler.modular.ImageStore;
import com.vnsearch.model.WebDocument;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MultiDomainCrawlRunner {

    private static final List<String> VIETNAMESE_SEEDS = List.of(
            "https://vnexpress.net/",
            "https://tuoitre.vn/",
            "https://dantri.com.vn/",
            "https://thanhnien.vn/",
            "https://vietnamnet.vn/",
            "https://nhandan.vn/",
            "https://hanoimoi.vn/",
            "https://baochinhphu.vn/",
            "https://www.vietnamplus.vn/",
            "https://tuyensinhso.vn/",
            "https://hcmiu.edu.vn/");

    private static final List<String> ENGLISH_SEEDS = List.of(
            "https://e.vnexpress.net/",
            "https://en.vietnamnet.vn/",
            "https://en.nhandan.vn/",
            "https://en.baochinhphu.vn/",
            "https://en.vietnamplus.vn/",
            "https://vietnamnews.vn/",
            "https://english.vov.vn/",
            "https://vir.com.vn/");

    private static final List<String> DEFAULT_SEEDS = concat(VIETNAMESE_SEEDS, ENGLISH_SEEDS);

    private static final Set<String> LANGUAGE_LABELS = Set.of("www", "e", "en");

    public static void main(String[] args) throws IOException {
        int maxPages = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        int maxDepth = args.length > 1 ? Integer.parseInt(args[1]) : 3;
        String outputPath = args.length > 2 ? args[2] : "data/crawled-multi.json";
        boolean fresh = args.length > 3 && args[3].equalsIgnoreCase("--fresh");

        List<WebDocument> previous = List.of();
        if (!fresh && Files.exists(Path.of(outputPath))) {
            previous = ContentStorage.loadFromJson(outputPath);
            System.out.printf("Noi tiep corpus san co: %d tai lieu tu %s%n",
                    previous.size(), outputPath);
        }

        ImageStore imageStore = new ImageStore();
        String imagePath = ImageStorage.pathFor(outputPath);
        if (!fresh) {
            List<ImageFound> previousImages = ImageStorage.loadQuietly(imagePath);
            if (!previousImages.isEmpty()) {
                imageStore.addAll(previousImages);
                System.out.printf("Noi tiep kho anh san co : %d anh tu %s%n",
                        previousImages.size(), imagePath);
            }
        }

        Set<String> allowedDomains = new LinkedHashSet<>();
        for (String seed : DEFAULT_SEEDS) {
            String host = URI.create(seed).getHost();
            if (host != null) {
                allowedDomains.add(stripLanguageLabel(host));
            }
        }

        System.out.println("=== CRAWL DA DOMAIN ===");
        System.out.printf("Seeds      : %d (%d tieng Viet + %d tieng Anh) tren %d domain%n",
                DEFAULT_SEEDS.size(), VIETNAMESE_SEEDS.size(), ENGLISH_SEEDS.size(),
                allowedDomains.size());
        System.out.println("Ngon ngu   : CHI tieng Viet va tieng Anh (LanguageFilter)");
        System.out.println("maxPages   : " + maxPages);
        System.out.println("maxDepth   : " + maxDepth);
        System.out.println("Output     : " + outputPath);
        System.out.println();

        CrawlConfig config = CrawlConfig.builder()
                .maxDepth(maxDepth)
                .maxPages(maxPages)
                .threadCount(Math.min(32, distinctSeedHosts() * 2))
                .allowedDomains(allowedDomains)
                .excludedHostPrefixes(UrlFilter.NON_VI_EN_HOST_PREFIXES)
                .maxDurationMinutes(180)
                .build();

        CrawlerService crawler = new CrawlerService(null, imageStore);
        crawler.addListener(new ProgressBarCrawlListener(25))
                .addListener(new ConsoleCrawlListener(200))
                .addListener(new CheckpointCrawlListener(
                        crawler::snapshotDocuments, imageStore::all, outputPath, 250));
        long start = System.currentTimeMillis();

        List<WebDocument> docs = crawler.crawl(DEFAULT_SEEDS, config, previous);

        long elapsedMs = System.currentTimeMillis() - start;


        ContentStorage.saveToJson(docs, outputPath);
        List<ImageFound> images = imageStore.all();
        ImageStorage.saveToJson(images, imagePath);
        System.out.printf("Kho anh    : %d anh tren %d trang -> %s%n",
                images.size(), imageStore.pageCount(), imagePath);

        printBlockStatistics(crawler);
        printStatistics(docs, elapsedMs, outputPath, allowedDomains);
    }

    private static List<String> concat(List<String> a, List<String> b) {
        List<String> all = new ArrayList<>(a);
        all.addAll(b);
        return List.copyOf(all);
    }

    private static int distinctSeedHosts() {
        Set<String> hosts = new LinkedHashSet<>();
        for (String seed : DEFAULT_SEEDS) {
            String host = URI.create(seed).getHost();
            if (host != null) {
                hosts.add(host);
            }
        }
        return hosts.size();
    }

    private static String stripLanguageLabel(String host) {
        int dot = host.indexOf('.');
        if (dot <= 0) {
            return host;
        }
        String first = host.substring(0, dot).toLowerCase(Locale.ROOT);
        String rest = host.substring(dot + 1);
        if (LANGUAGE_LABELS.contains(first) && rest.indexOf('.') > 0) {
            return rest;
        }
        return host;
    }

    private static void printBlockStatistics(CrawlerService crawler) {
        System.out.println();
        System.out.println("=== THONG KE THEO TUNG KHOI ===");

        DnsResolver dns = crawler.getDnsResolver();
        System.out.printf("DNS Resolver   : %d host trong cache, ty le trung %.1f%%, %d host chet bi loai som%n",
                dns.getCachedHostCount(), dns.hitRate() * 100, dns.getResolveFailures());

        HtmlDownloader downloader = crawler.getHtmlDownloader();
        System.out.printf("HTML Downloader: tai %d trang, %d lan thu lai, %d that bai%n",
                downloader.getDownloadedCount(), downloader.getRetryCount(),
                downloader.getFailedCount());

        LanguageFilter language = crawler.getLanguageFilter();
        System.out.printf("Language Filter: GIU %d tieng Viet + %d tieng Anh + %d chua ro, VUT %d ngoai ngu%n",
                language.getAcceptedVietnameseCount(), language.getAcceptedEnglishCount(),
                language.getAcceptedUndeterminedCount(), language.getRejectedCount());
        Map<String, Long> rejectedByLanguage = language.getRejectedByLanguage();
        if (!rejectedByLanguage.isEmpty()) {
            StringBuilder line = new StringBuilder("                 (");
            rejectedByLanguage.forEach((code, count) -> line.append(code).append(' ')
                    .append(count).append(" | "));
            line.setLength(line.length() - 3);
            System.out.println(line.append(')'));
        }

        ContentSeenFilter contentSeen = crawler.getContentSeenFilter();
        System.out.printf("Content Seen?  : %d noi dung phan biet, VUT %d ban trung, %d trang than bai rong%n",
                contentSeen.size(), contentSeen.getDuplicateCount(), contentSeen.getBlankSkippedCount());

        UrlFilter filter = crawler.getUrlFilter();
        System.out.printf("URL Filter     : nhan %d, loai %d%n",
                filter.getAcceptedCount(), filter.getTotalRejectedCount());
        System.out.printf("                 (domain %d | tien to host %d | duoi tep %d | do sau %d "
                        + "| scheme %d | robots %d)%n",
                filter.getRejectedByDomainCount(), filter.getRejectedByHostPrefixCount(),
                filter.getRejectedByExtensionCount(), filter.getRejectedByDepthCount(),
                filter.getRejectedBySchemeCount(), filter.getRejectedByRobotsCount());

        UrlSeenFilter urlSeen = crawler.getUrlSeenFilter();
        System.out.printf("URL Seen?      : %d URL phan biet, bo loc %d bit (%.1f KB), %d ham bam%n",
                urlSeen.getSeenCount(), urlSeen.getNumBits(),
                urlSeen.getNumBits() / 8192.0, urlSeen.getNumHashes());

        UrlStorage urlStorage = urlSeen.getUrlStorage();
        System.out.printf("URL Storage    : %s%n", urlStorage.isEnabled()
                ? urlStorage.getWrittenCount() + " URL da ghi vao " + urlStorage.getPath()
                : "tat (dung CrawlConfig.urlStoragePath de bat)");
    }

    private static void printStatistics(List<WebDocument> docs, long elapsedMs,
                                         String outputPath, Set<String> allowedDomains) {
        System.out.println();
        System.out.println("=== THONG KE CRAWL ===");
        System.out.printf("Tong so trang    : %d%n", docs.size());
        System.out.printf("Thoi gian        : %.1f phut%n", elapsedMs / 60000.0);
        System.out.printf("Thong luong      : %.2f trang/giay%n", docs.size() / (elapsedMs / 1000.0));

        long totalOutlinks = docs.stream().mapToInt(d -> d.getOutlinks().size()).sum();
        System.out.printf("Tong outlink     : %d (trung binh %.1f/trang)%n",
                totalOutlinks, docs.isEmpty() ? 0 : (double) totalOutlinks / docs.size());

        Map<String, Integer> perDomain = new LinkedHashMap<>();
        for (WebDocument doc : docs) {
            perDomain.merge(hostOf(doc.getUrl()), 1, Integer::sum);
        }
        System.out.println("Phan bo theo domain:");
        perDomain.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .forEach(e -> System.out.printf("  %-24s %5d trang%n", e.getKey(), e.getValue()));

        Map<String, Integer> perLanguage = new LinkedHashMap<>();
        for (WebDocument doc : docs) {
            String lang = doc.getLanguage() == null || doc.getLanguage().isBlank()
                    ? "(chua gan)" : doc.getLanguage();
            perLanguage.merge(lang, 1, Integer::sum);
        }
        System.out.println("Phan bo theo ngon ngu:");
        perLanguage.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .forEach(e -> System.out.printf("  %-24s %5d trang (%.1f%%)%n", e.getKey(),
                        e.getValue(), docs.isEmpty() ? 0 : 100.0 * e.getValue() / docs.size()));

        Set<String> crawledUrls = new LinkedHashSet<>();
        for (WebDocument doc : docs) {
            crawledUrls.add(doc.getUrl());
        }
        long crossDomainLinks = 0;
        long internalLinks = 0;
        long danglingLinks = 0;
        for (WebDocument doc : docs) {
            String from = hostOf(doc.getUrl());
            for (String outlink : doc.getOutlinks()) {
                if (!crawledUrls.contains(outlink)) {
                    danglingLinks++;
                }
                if (hostOf(outlink).equals(from)) {
                    internalLinks++;
                } else {
                    crossDomainLinks++;
                }
            }
        }
        long edges = internalLinks + crossDomainLinks;
        System.out.printf("Canh do thi (nnz): %d (noi bo %d, CHEO domain %d)%n",
                edges, internalLinks, crossDomainLinks);
        System.out.printf("Canh treo         : %d (%.1f%%) - tro toi trang CHUA co trong corpus%n",
                danglingLinks, edges == 0 ? 0.0 : 100.0 * danglingLinks / edges);
        if (!docs.isEmpty()) {
            double density = (double) edges / ((double) docs.size() * docs.size());
            System.out.printf("Ty le thua       : %.4f%% (nnz/n^2)%n", density * 100);
        }
        System.out.println("Da luu vao " + outputPath);

        List<String> missing = new ArrayList<>();
        for (String domain : allowedDomains) {
            if (perDomain.keySet().stream().noneMatch(h -> h.endsWith(domain))) {
                missing.add(domain);
            }
        }
        if (!missing.isEmpty()) {
            System.out.println("CANH BAO: khong crawl duoc trang nao tu " + missing);
        }
    }

    private static String hostOf(String url) {
        String host = DnsResolver.hostOf(url);
        return host != null ? host : "(khong ro)";
    }
}
