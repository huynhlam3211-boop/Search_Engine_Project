package com.vnsearch.crawler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public final class ProgressBarCrawlListener implements CrawlListener {

    private static final int BAR_WIDTH = 28;

    private static final long MIN_REPAINT_MS = 100;

    private static final String ESC_RESET = "\033[0m";
    private static final String ESC_BOLD = "\033[1m";
    private static final String ESC_GREEN = "\033[32m";

    private final Object lock = new Object();
    private final AtomicInteger errors = new AtomicInteger();
    private final AtomicInteger duplicates = new AtomicInteger();

    private final boolean interactive;
    private final boolean unicode;
    private final boolean color;
    private final int everyN;
    private final long startMs;

    private int lastLineLength;
    private long lastRepaintMs;

    public ProgressBarCrawlListener(int everyN) {
        this.everyN = Math.max(1, everyN);
        this.interactive = detectInteractive();
        this.unicode = stdoutCharset().newEncoder().canEncode("█░")
        this.color = interactive && System.getenv("NO_COLOR") == null;
        this.startMs = System.currentTimeMillis();
    }

    private static boolean detectInteractive() {
        String forced = System.getProperty("crawl.progress");
        if (forced != null) {
            return forced.equalsIgnoreCase("bar");
        }
        return System.console() != null;
    }

    private static Charset stdoutCharset() {
        for (String key : new String[] {"stdout.encoding", "native.encoding", "file.encoding"}) {
            String name = System.getProperty(key);
            if (name == null) {
                continue;
            }
            try {
                return Charset.forName(name);
            } catch (RuntimeException ignored) {
            }
        }
        return StandardCharsets.UTF_8;
    }

    @Override
    public void onPageCrawled(CrawlEvent e) {
        synchronized (lock) {
            if (!interactive) {
                if (e.pageNumber() % everyN == 0 || e.pageNumber() == e.maxPages()) {
                    System.out.println(plainLine(e, System.currentTimeMillis()));
                }
                return;
            }
            long now = System.currentTimeMillis();
            if (now - lastRepaintMs < MIN_REPAINT_MS && e.pageNumber() != e.maxPages()) {
                return;
            }
            lastRepaintMs = now;
            paint(e, now);
        }
    }

    @Override
    public void onError(String url, Exception error) {
        errors.incrementAndGet();
    }

    @Override
    public void onDuplicateContent(String url) {
        duplicates.incrementAndGet();
    }

    @Override
    public void onFinished(int totalPages, long elapsedMs) {
        synchronized (lock) {
            if (interactive) {
                clearLine();
            }
            double seconds = elapsedMs / 1000.0;
            System.out.printf(Locale.US,
                    "%s %d trang trong %s (%.2f trang/giây) — %d lỗi, %d trùng nội dung%n",
                    unicode ? "✓" : "OK", totalPages, formatDuration(elapsedMs),
                    seconds > 0 ? totalPages / seconds : 0.0,
                    errors.get(), duplicates.get());
        }
    }

    private void paint(CrawlEvent e, long now) {
        String plain = plainLine(e, now);
        String shown = color ? colorLine(e, now) : plain;

        int padding = Math.max(0, lastLineLength - plain.length());
        System.out.print("\r" + shown + " ".repeat(padding));
        System.out.flush();
        lastLineLength = plain.length();
    }

    private void clearLine() {
        System.out.print("\r" + " ".repeat(lastLineLength) + "\r");
        System.out.flush();
        lastLineLength = 0;
    }

    private String plainLine(CrawlEvent e, long now) {
        return bar(e) + " " + stats(e, now);
    }

    private String colorLine(CrawlEvent e, long now) {
        return ESC_GREEN + bar(e) + ESC_RESET + " " + ESC_BOLD + stats(e, now) + ESC_RESET;
    }

    private String bar(CrawlEvent e) {
        double ratio = e.maxPages() <= 0 ? 0 : Math.min(1.0, (double) e.pageNumber() / e.maxPages());
        int filled = (int) Math.round(ratio * BAR_WIDTH);
        String full = unicode ? "█" : "#";
        String empty = unicode ? "░" : ".";
        return "[" + full.repeat(filled) + empty.repeat(BAR_WIDTH - filled) + "]"
                + String.format(Locale.US, " %3.0f%%", ratio * 100);
    }

    private String stats(CrawlEvent e, long now) {
        long elapsedMs = now - startMs;
        double rate = elapsedMs > 0 ? e.pageNumber() * 1000.0 / elapsedMs : 0.0;
        int remaining = Math.max(0, e.maxPages() - e.pageNumber());
        String eta = rate > 0 ? formatDuration((long) (remaining / rate * 1000)) : "--:--";

        return String.format(Locale.US,
                "%d/%d  %.1f trang/s  còn ~%s  hàng đợi %s  %d host  %d lỗi  %d trùng",
                e.pageNumber(), e.maxPages(), rate, eta,
                compact(e.frontierSize()), e.domainCount(), errors.get(), duplicates.get());
    }

    private static String compact(int value) {
        if (value < 10_000) {
            return Integer.toString(value);
        }
        return String.format(Locale.US, "%.1fk", value / 1000.0);
    }

    private static String formatDuration(long ms) {
        long totalSeconds = Math.max(0, ms / 1000);
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes >= 60) {
            return String.format(Locale.US, "%d:%02d:%02d", minutes / 60, minutes % 60, seconds);
        }
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }
}
