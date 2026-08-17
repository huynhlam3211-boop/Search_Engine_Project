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
    public static final String VIETNAMESE = "vi"
    public static final String ENGLIST = "en"
    public static final String UNDETERMINED = "und"
    public static final String OTHER_LATIN = "other"

    private static final int SAMPLE_LIMIT = 20_000;

    /** Tỷ lệ chữ cái thuộc hệ chữ khác đủ để kết luận trang không phải vi/en. */
    private static final double FOREIGN_SCRIPT_THRESHOLD = 0.10;

    /** Tỷ lệ ký tự mang dấu đặc trưng tiếng Việt đủ để kết luận là tiếng Việt. */
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

    }

    public String detect(String declaredLang, String text) {

    }

    public static String normalizeLanguageTag(String tag) {

    }

    public static boolean isViorEn(String code) {

    }

    public long getAcceptedVietnameseCount() {

    }

    public long getAcceptedEnglishCount() {

    }

    public long getAcceptedUndeterminedCount(){

    }

    public long getRejectedCount() {

    }

    public Map<String, Long> getRejectedByLanguage() {
        
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