package com.vnsearch.crawler;

import com.vnsearch.model.WebDocument;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class LanguageFilter {
    public static final String VIETNAMESE = "vi";
    public static final String ENGLISH = "en";
    public static final String UNDETERMINED = "und";
    public static final String OTHER_LATIN = "other";

    private static final int SAMPLE_LIMIT = 20_000;

    /** Tỷ lệ chữ cái thuộc hệ chữ khác đủ để kết luận trang không phải vi/en. */
    private static final double FOREIGN_SCRIPT_THRESHOLD = 0.10;

    /**
     * Tỷ lệ ký tự mang dấu ĐỦ DÀY để một mình nó kết luận là tiếng Việt.
     *
     * <p>Văn xuôi tiếng Việt thật đạt 20–30%. Ngưỡng 5% để lại biên rất rộng cho
     * trang ít văn xuôi, nhưng vẫn nằm TRÊN mức mà một trang tiếng Anh viết về Việt
     * Nam đạt được chỉ nhờ tên riêng có dấu — đo trên vietnamnews.vn là ~2,9%
     * ({@code Việt Nam}, {@code Hà Nội}, {@code Đắk Lắk}...). Ngưỡng cũ 0,5% khiến
     * đúng trang đó bị gán nhãn {@code vi}.
     */
    private static final double VIETNAMESE_DIACRITIC_STRONG = 0.05;

    /**
     * Tỷ lệ ký tự mang dấu đủ để NGHIÊNG về tiếng Việt khi không còn bằng chứng nào
     * khác — dùng làm chốt cuối, sau khi phép đếm từ chức năng đã thất bại cho cả hai
     * ngôn ngữ. Các ký tự này chỉ xuất hiện trong tiếng Việt, nên dù thưa chúng vẫn
     * đáng tin hơn {@code <html lang>}.
     */
    private static final double VIETNAMESE_DIACRITIC_THRESHOLD = 0.005;

    /** Tỷ lệ token là từ chức năng tiếng Việt đủ để kết luận là tiếng Việt. */
    private static final double VIETNAMESE_WORD_THRESHOLD = 0.05;

    /** Tỷ lệ token là từ chức năng tiếng Anh đủ để kết luận là tiếng Anh. */
    private static final double ENGLISH_WORD_THRESHOLD = 0.12;

    /**
     * Ngưỡng nới cho tiếng Anh khi {@code <html lang>} cũng khai là tiếng Anh
     * — hai bằng chứng yếu độc lập cộng lại đủ mạnh.
     */
    private static final double ENGLISH_WORD_THRESHOLD_WITH_HINT = 0.05;

    /** Dưới mức này thì văn bản quá ngắn để kết luận; rơi về {@code <html lang>}. */
    private static final int MIN_TOKENS_FOR_CONTENT_EVIDENCE = 40;

    private static final Set<Character> VIETNAMESE_ONLY_CHARS = Set.of(
            'ơ', 'ư', 'ă', 'đ', 'Ơ', 'Ư', 'Ă', 'Đ');

    private static final Set<String> VIETNAMESE_FUNCTION_WORDS = Set.of(
            "của", "và", "là", "được", "trong", "người", "những", "các", "có",
            "không", "cho", "với", "để", "này", "đã", "khi", "một", "đến", "về",
            "như", "từ", "cũng", "thì", "sẽ", "tại", "theo", "đó", "nhiều",
            "năm", "trên", "ở", "vào", "nhưng", "hơn", "phải", "làm", "việc");

    private static final Set<String> ENGLISH_FUNCTION_WORDS = Set.of(
            "the", "of", "and", "to", "in", "is", "that", "for", "it", "with",
            "was", "on", "are", "be", "this", "have", "from", "has", "not",
            "but", "they", "which", "said", "will", "would", "about", "more",
            "been", "were", "their", "its", "than", "when", "who", "what",
            "into", "also", "after", "over", "only", "other", "these", "such",
            "there", "his", "her", "our", "you", "he", "she", "we", "at", "by");

    /** Mã ngôn ngữ tiêu biểu cho từng hệ chữ viết, để thống kê đọc được. */
    private static final Map<Character.UnicodeScript, String> SCRIPT_LANGUAGE =
            new EnumMap<>(Map.of(
                    Character.UnicodeScript.HAN, "zh",
                    Character.UnicodeScript.HIRAGANA, "ja",
                    Character.UnicodeScript.KATAKANA, "ja",
                    Character.UnicodeScript.HANGUL, "ko",
                    Character.UnicodeScript.CYRILLIC, "ru",
                    Character.UnicodeScript.ARABIC, "ar",
                    Character.UnicodeScript.THAI, "th",
                    Character.UnicodeScript.DEVANAGARI, "hi",
                    Character.UnicodeScript.HEBREW, "he",
                    Character.UnicodeScript.GREEK, "el"));

    private final AtomicLong acceptedVietnamese = new AtomicLong();
    private final AtomicLong acceptedEnglish = new AtomicLong();
    private final AtomicLong acceptedUndetermined = new AtomicLong();
    private final AtomicLong rejected = new AtomicLong();

    private final Map<String, AtomicLong> rejectedByLanguage = new ConcurrentHashMap<>();

    public boolean accept(WebDocument doc) {
        if (doc == null) {
            return false;
        }
        // Ghép tiêu đề với thân bài: trang danh mục có thân bài rất ngắn,
        // tiêu đề khi đó là phần văn bản đáng tin duy nhất.
        String text = (doc.getTitle() == null ? "" : doc.getTitle() + " ")
                + (doc.getBodyText() == null ? "" : doc.getBodyText());
        String language = detect(doc.getLanguage(), text);
        doc.setLanguage(language);

        switch (language) {
            case VIETNAMESE -> acceptedVietnamese.incrementAndGet();
            case ENGLISH -> acceptedEnglish.incrementAndGet();
            case UNDETERMINED -> acceptedUndetermined.incrementAndGet();
            default -> {
                rejected.incrementAndGet();
                rejectedByLanguage.computeIfAbsent(language, k -> new AtomicLong())
                        .incrementAndGet();
                return false;
            }
        }
        return true;
    }

    public String detect(String declaredLang, String text) {
        String hint = normalizeLanguageTag(declaredLang);
        if (text == null || text.isBlank()) {
            return isViOrEn(hint) ? hint : UNDETERMINED;
        }
        String sample = text.length() > SAMPLE_LIMIT ? text.substring(0, SAMPLE_LIMIT) : text;

        // --- Tầng 1: hệ chữ viết ---
        Map<String, Integer> foreignByLanguage = new HashMap<>();
        int letters = 0;
        int foreignLetters = 0;
        int vietnameseMarks = 0;
        for (int i = 0; i < sample.length(); i++) {
            char c = sample.charAt(i);
            if (!Character.isLetter(c)) {
                continue;
            }
            letters++;
            if ((c >= 'Ạ' && c <= 'ỹ') || VIETNAMESE_ONLY_CHARS.contains(c)) {
                vietnameseMarks++;
                continue; // chắc chắn là chữ Latinh, khỏi tra bảng script
            }
            Character.UnicodeScript script = Character.UnicodeScript.of(c);
            if (script == Character.UnicodeScript.LATIN
                    || script == Character.UnicodeScript.COMMON
                    || script == Character.UnicodeScript.INHERITED) {
                continue;
            }
            foreignLetters++;
            String lang = SCRIPT_LANGUAGE.getOrDefault(
                    script, script.name().toLowerCase(Locale.ROOT));
            foreignByLanguage.merge(lang, 1, Integer::sum);
        }
        if (letters == 0) {
            return isViOrEn(hint) ? hint : UNDETERMINED;
        }
        if ((double) foreignLetters / letters > FOREIGN_SCRIPT_THRESHOLD) {
            return foreignByLanguage.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(OTHER_LATIN);
        }

        // --- Tầng 2: dấu phụ đặc trưng tiếng Việt ---
        // Chỉ kết luận ngay khi dấu thanh DÀY ĐẶC. Dấu thanh THƯA thì chưa đủ: một
        // trang tiếng Anh viết về Việt Nam cũng đạt mức đó chỉ nhờ tên riêng có dấu.
        // Ca đó được nhường cho tầng 3 phân xử; dấu thanh thưa quay lại làm chốt cuối.
        double diacriticRatio = (double) vietnameseMarks / letters;
        if (diacriticRatio >= VIETNAMESE_DIACRITIC_STRONG) {
            return VIETNAMESE;
        }

        // --- Tầng 3: từ chức năng ---
        String[] tokens = sample.toLowerCase(Locale.ROOT).split("[^\\p{L}]+");
        int total = 0;
        int viHits = 0;
        int enHits = 0;
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            total++;
            if (VIETNAMESE_FUNCTION_WORDS.contains(token)) {
                viHits++;
            } else if (ENGLISH_FUNCTION_WORDS.contains(token)) {
                enHits++;
            }
        }
        if (total < MIN_TOKENS_FOR_CONTENT_EVIDENCE) {
            // Quá ngắn để đếm từ chức năng. Dấu thanh dù thưa vẫn là dấu hiệu RIÊNG
            // của tiếng Việt, đáng tin hơn <html lang> — giữ nguyên hành vi cũ cho
            // tiêu đề ngắn kiểu "Trang chủ", "Tin tức trong nước".
            if (diacriticRatio >= VIETNAMESE_DIACRITIC_THRESHOLD) {
                return VIETNAMESE;
            }
            // Không có dấu nào: tin tạm <html lang>, không có thì cho qua.
            return isViOrEn(hint) ? hint : UNDETERMINED;
        }
        if ((double) viHits / total >= VIETNAMESE_WORD_THRESHOLD) {
            return VIETNAMESE;
        }
        double englishRatio = (double) enHits / total;
        if (englishRatio >= ENGLISH_WORD_THRESHOLD
                || (ENGLISH.equals(hint) && englishRatio >= ENGLISH_WORD_THRESHOLD_WITH_HINT)) {
            return ENGLISH;
        }
        // Chốt cuối: không đủ từ chức năng của CẢ HAI ngôn ngữ, nhưng có dấu thanh.
        // Đây là trang liệt kê, trang nhiều tên riêng, trang ít văn xuôi tiếng Việt —
        // tầng 2 cũ bắt được chúng, và chốt này giữ nguyên kết quả đó.
        if (diacriticRatio >= VIETNAMESE_DIACRITIC_THRESHOLD) {
            return VIETNAMESE;
        }
        // Chữ Latinh, đủ dài, không dấu thanh, không dấu hiệu của cả hai: Pháp, Đức,
        // Indonesia, Tây Ban Nha... — đúng thứ chính sách này loại.
        return OTHER_LATIN;
    }

    public static String normalizeLanguageTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return "";
        }
        String lower = tag.trim().toLowerCase(Locale.ROOT);
        int dash = lower.indexOf('-');
        if (dash > 0) {
            lower = lower.substring(0, dash);
        }
        int underscore = lower.indexOf('_');
        if (underscore > 0) {
            lower = lower.substring(0, underscore);
        }
        return lower;
    }

    private static boolean isViOrEn(String code) {
        return VIETNAMESE.equals(code) || ENGLISH.equals(code);
    }

    public long getAcceptedVietnameseCount() {
        return acceptedVietnamese.get();
    }

    public long getAcceptedEnglishCount() {
        return acceptedEnglish.get();
    }

    public long getAcceptedUndeterminedCount() {
        return acceptedUndetermined.get();
    }

    public long getRejectedCount() {
        return rejected.get();
    }

    public Map<String, Long> getRejectedByLanguage() {
        Map<String, Long> snapshot = new LinkedHashMap<>();
        rejectedByLanguage.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .forEach(e -> snapshot.put(e.getKey(), e.getValue().get()));
        return snapshot;
    }

    /** Demo*/
    public static void main(String[] args) {
        LanguageFilter filter = new LanguageFilter();

        String vi = "Đội tuyển Việt Nam giành chiến thắng trong trận đấu tối qua tại sân "
                + "vận động quốc gia, với hai bàn thắng được ghi trong hiệp hai của trận đấu "
                + "mà người hâm mộ cả nước đã chờ đợi từ nhiều tháng nay.";
        String en = "The national team won the match last night at the stadium, with two "
                + "goals that were scored in the second half of a game which fans from all "
                + "over the country had been waiting for over the last few months.";
        String zh = "越南国会常务委员会会议提交国会审议通过设立广宁市和北宁市的决议，"
                + "会议还讨论了其他若干重要议题，并就下一阶段的工作作出安排。";
        String fr = "Le championnat national de football a repris ses droits avec une "
                + "rencontre disputée hier soir dans le stade de la capitale, devant des "
                + "milliers de supporters venus de toutes les régions du pays.";

        System.out.println("Tiếng Việt : " + filter.detect("", vi));
        System.out.println("Tiếng Anh  : " + filter.detect("", en));
        System.out.println("Tiếng Trung: " + filter.detect("", zh) + "  <- bị loại");
        System.out.println("Tiếng Pháp : " + filter.detect("", fr) + "  <- bị loại");
        System.out.println("Trang ngắn : " + filter.detect("en", "Trang chủ") + "  <- theo <html lang>");
    }

}