package com.vnsearch.crawler.modular;

import com.vnsearch.crawler.bus.ImageFound;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ImageQuality {

    private static final Pattern DECORATIVE_EXTENSION =
            Pattern.compile("\\.(svg|gif|ico|bmp)(\\?|#|$)", Pattern.CASE_INSENSITIVE);

    private static final Pattern DECORATIVE_PATH = Pattern.compile(
            "thumb|icon|logo|avatar|sprite|placeholder|blank|banner|badge|button|" +
            "favicon|watermark|1x1|pixel|spacer");

    private static final Pattern WIDTH_PARAM =
            Pattern.compile("[?&](?:w|width|rw|mw)=(\\d{2,4})", Pattern.CASE_INSENSITIVE);

    private static final Pattern SIZE_IN_PATH =
            Pattern.compile("[_\\-/](\\d{3,4})x(\\d{3,4})[_\\-./]");

    private static final int MIN_CONTENT_WIDTH = 200;

    private ImageQuality() {
    }

    // --- Ba bậc, từ tốt nhất xuống ------------------------------------------

    private static final int TIER_SIZED_CONTENT = 3;

    private static final int TIER_UNKNOWN = 2;

    private static final int TIER_SMALL = 1;

    private static final int TIER_DECORATIVE = 0;

    public static int compare(ImageFound a, ImageFound b) {
        if (b == null) {
            return 1;
        }
        if (a == null) {
            return -1;
        }

        int tierDiff = tier(a) - tier(b);
        if (tierDiff != 0) {
            return tierDiff;
        }

        int widthDiff = estimatedWidth(a) - estimatedWidth(b);
        if (widthDiff != 0) {
            return widthDiff;
        }

        return Boolean.compare(!a.missingAlt(), !b.missingAlt());
    }

    public static boolean isBetter(ImageFound candidate, ImageFound current) {
        return compare(candidate, current) > 0;
    }

    private static int tier(ImageFound image) {
        String url = image.imageUrl().toLowerCase(java.util.Locale.ROOT);

        if (DECORATIVE_EXTENSION.matcher(url).find() || DECORATIVE_PATH.matcher(url).find()) {
            return TIER_DECORATIVE;
        }

        int width = estimatedWidth(image);
        if (width <= 0) {
            return TIER_UNKNOWN;
        }
        return width >= MIN_CONTENT_WIDTH ? TIER_SIZED_CONTENT : TIER_SMALL;
    }

    static int estimatedWidth(ImageFound image) {
        if (image.declaredWidth() > 0) {
            return image.declaredWidth();
        }

        String url = image.imageUrl();

        Matcher param = WIDTH_PARAM.matcher(url);
        if (param.find()) {
            return parseOrZero(param.group(1));
        }

        Matcher inPath = SIZE_IN_PATH.matcher(url);
        if (inPath.find()) {
            return parseOrZero(inPath.group(1));
        }

        return 0;
    }

    private static int parseOrZero(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
