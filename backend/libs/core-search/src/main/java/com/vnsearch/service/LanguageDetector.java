package com.vnsearch.service;

import com.vnsearch.crawler.LanguageFilter;

/**
 * Bo phat hien ngon ngu gon nhe cho tang service.
 *
 * <p>Uy quyen cho {@link LanguageFilter} de chi co MOT bo quy tac nhan dang
 * tieng Viet trong toan he thong.
 */
public final class LanguageDetector {

    public static final int MIN_LENGTH_TO_JUDGE = 15;

    private static final LanguageFilter FILTER = new LanguageFilter();

    private LanguageDetector() {
    }

    /**
     * @return true neu {@code text} du dai de phan xet va duoc nhan la tieng Viet
     */
    public static boolean looksVietnamese(String text) {
        if (text == null || text.strip().length() < MIN_LENGTH_TO_JUDGE) {
            return false;
        }
        return LanguageFilter.VIETNAMESE.equals(FILTER.detect("", text));
    }
}
