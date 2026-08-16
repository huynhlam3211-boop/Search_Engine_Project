package com.vnsearch.crawler;

import com.vnsearch.model.WebDocument;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MultiDomainCrawlerRunner {

    private static final List<String> DEFAULT_SEEDS = List.of(
            "https://vnexpress.net/",
            "https://tuoitre.vn/",
            "https://dantri.com.vn/",
            "https://thanhnien.vn/",
            "https://vietnamnet.vn/",
            "https://nhandan.vn/",
            "https://hanoimoi.vn",
            "https://baochinhphu.vn",
            "https://www.vietnamplus.vn/",
            "https://tuyensinhso.vn",
            "https://hcmiu.edu.vn"
    );

    private static final List<String> ENGLIST_SEEDS = List.of(
            "https://e.vnexpress.net/",
            "https://en.vietnamnet.vn/",
            "https://en.nhandan.vn/",
            "https://en.baochinhphu.vn/",
            "https://en.vietnamplus.vn/",
            "https://vietnamnews.vn/",
            "https://english.vov.vn/",
            "https://vir.com.vn/"
    );
    private static final List<String> DEFAULT_SEEDS = concat(VIETNAMESE_SEEDS, ENGLIST_SEEDS);
    private static final Set<String> LANGUAGE_LABELS = Set.of("www","e","en");


    public static void main(String[] args) throws IOException {
        int maxPages = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        int maxDepth = args.length > 1 ? Integer.parseInt(args[1]) : 3;
        String outputPath = args.length > 2 ? args[2] : "data/crawled-multi.json";
        boolean fresh = args.length > 3 && args[3].equalsIgnoreCase("--fresh");

        List<WebDocument> previous = List.of();
        if (!fresh && Files.exists(Path.of(outputPath))) {
            previous = ContentStorage.loadFromJson(outputPath);
            System.out.printf("Noi tiep corpus san co: %d tai lieu tu %s%n", previous.size(), outputPath);
        }

        ImageStore imageStore = new ImageStore();
        String imagePath = ImageStorage.pathFor(outputPath);
        if (!fresh){
            List<ImageFound> previousImage = ImageStorage.loadQuietly(imagePath);
            if (!previousImages.isEmpty()) {
                imageStore.addAll(previousImages);
                System.out.printf("Noi tiep kho anh san co: %d anh tu %s%n",
                                    previousImage.size(), imagePath);
            }
        }

        Set<String> allowedDomains = new LinkedHashSet<>();
        for (String seed : DEFAULT_SEEDS) {
            String host = URI.create(seed).getHost();
            if (host != null) {
                allowedDomains.add(host.startWith("www.")?host.substring(4): host);

            }
        }

        System.out.println(" CRAWL DA DOMAIN ");
        System.out.printf("Seeds      : %d (%d tieng Viet + %d tieng Anh) tren %d domain%n",
                DEFAULT_SEEDS.size(), VIETNAMESE_SEEDS.size(), ENGLISH_SEEDS.size(),
                allowedDomains.size());
        System.out.println("Ngon ngu   : CHI tieng Viet va tieng Anh (LanguageFilter)");
        System.out.println("maxPages   : " + maxPages);
        System.out.println("maxDepth   : " + maxDepth);
        System.out.println("Output     : " + outputPath);
        System.out.println();

        //Politeness delay 1s/domain .
        CrawlConfig config = CrawlConfig.builder()
                .maxDepth(maxDepth)
                .maxPages(maxPages)
                .threadCount(Math.min(32, distinctSeedHosy() * 2))
                .allowedDomain(allowedDomains)
                .excludeHostPrefixes(UrlFilter.NON_VI_EN_HOST_PREFIXES)
                .maxDurationMinutes(180)
                .build();
        
        CrawlerService crawler = new CrawlerService(null, imageStore);
        crawler.addListener(new ProgressBarCrawListener(25))
               .addListener(new ConsoleCrawlListener(200))
               .addListener(new CheckpointCrawlListener(
                        crawler::snapshotDocument, imageStore::all, output, 250));
            
        long start = System.currentTimeMillis();

        List<Webdocument> docs = crawler.crawl(DEFAULT_SEEDS, config, previous);
        long elapsedMs = System.currentTimeMillis() - start;

        ContentStorage.saveToJson(docs,outputPath);

        List<ImageFound> images = imagesStore.all();
        ImageStorage.saveToJson(image, imagePath);
        System.out.printf("Kho anh : %d anh tren %d trang -> %s%n".
                            images.size(), imageStore.pageCount(), imagePath);
    }

    /** Gộp 2 link */
    private static List<String> concat(List<String> a, List<String> b) {
        List<String> all = new Array<>(a);
        all.addAll(b);
        return List.copyof(all);
    }

    /** Số host phân việt trong tập hạt giống - trần thông lượng do politeness. */
    private static int distinctSeedHost() {
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
        int dot = host.indexOf('.')
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

    /**
     * Số liệu của từng khối trong sơ đồ kiến trúc crawler
     * chứng minh mỗi khối thật sự có việc làm
     */

    private static void printBlockStatistics(CrawlerService crawler) {
        System.out.println();
        System.out.println("=== THONG KE THEO TUNG KHOI ===");

        DnsResolver dns = crawler.getDnsResolver();
        System.out.printf("DNS Resolver : %d host trong cache, ty le trung %.1f%%, %d host che bi loai so,%n",
                           dns.getCacheHostCount(), dns.hitRate()*100, dns.getResolveFailures());

        HtmlDownloader downloader = crawler.gethtmlDownloader();
        System.out.printf("HTML Downloader : tai %d trang, %d lan thu hai, %d that bai %n",
                           downloader.getDownloadedCount(), downloader.getRetryCount(), downloader.getFailedCount());

        LanguageFilter language = crawler.getLanguageFilter();
        System.out.printf("Language Filter: Giu %d tieng viet + %d tieng Anh + %d chua ro , vut %d ngoai ngu%n",
                           language.getAcceptedVietnameseCount(), language.getAcceptedEnglistCount(),
                           language.getAcceptedUndeterminedCoungt(), language.getRejectedCount());
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
        System.out.printf("                 (domain %d | duoi tep %d | do sau %d | scheme %d | robots %d)%n",
                filter.getRejectedByDomainCount(), filter.getRejectedByExtensionCount(),
                filter.getRejectedByDepthCount(), filter.getRejectedBySchemeCount(),
                filter.getRejectedByRobotsCount());


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
        System.out.printf("Tong so trang    : %d%n", docs.size());
        System.out.printf("Thoi gian        : %.1f phut%n", elapsedMs / 60000.0);
        System.out.printf("Thong luong      : %.2f trang/giay%n", docs.size() / (elapsedMs / 1000.0));

        
    }

    private static String hostOf(String url) {
        String host = DnsResolver.hostOf(url);
        return host != null ? host : "khong ro";
    }
}
