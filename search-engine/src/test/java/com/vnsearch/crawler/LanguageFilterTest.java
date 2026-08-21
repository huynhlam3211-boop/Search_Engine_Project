package com.vnsearch.crawler;

import com.vnsearch.model.WebDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageFilterTest {

    private static final String VIETNAMESE_ARTICLE =
            "Đội tuyển Việt Nam đã giành chiến thắng trong trận đấu tối qua tại sân vận động "
            + "quốc gia, với hai bàn thắng được ghi trong hiệp hai của trận đấu mà người hâm "
            + "mộ cả nước đã chờ đợi từ nhiều tháng nay. Huấn luyện viên trưởng cho biết đội "
            + "sẽ tiếp tục tập luyện để chuẩn bị cho vòng đấu kế tiếp vào cuối tháng này.";

    private static final String ENGLISH_ARTICLE =
            "The national team won the match last night at the stadium, with two goals that "
            + "were scored in the second half of a game which fans from all over the country "
            + "had been waiting for over the last few months. The head coach said that the "
            + "team will keep training for the next round at the end of this month.";

    private static final String CHINESE_ARTICLE =
            "越南国会常务委员会会议提交国会审议通过设立广宁市和北宁市的决议，会议还讨论了其他若干"
            + "重要议题，并就下一阶段的工作作出安排，要求各有关部门认真落实会议精神，确保各项任务"
            + "按期完成，为国家经济社会发展作出更大贡献。";

    private static final String FRENCH_ARTICLE =
            "Le championnat national de football a repris ses droits avec une rencontre "
            + "disputée hier soir dans le stade de la capitale, devant des milliers de "
            + "supporters venus de toutes les régions du pays pour encourager leur équipe "
            + "favorite lors de cette soirée particulièrement attendue par les amateurs.";

    /**
     * Bài tiếng Anh của một tờ báo Việt Nam: văn xuôi hoàn toàn tiếng Anh, nhưng tên
     * riêng được viết CÓ DẤU. Đo trên vietnamnews.vn thật, tỷ lệ dấu đạt ~2,9% — vượt
     * ngưỡng 0,5% cũ và khiến trang bị gán nhãn {@code vi}.
     */
    private static final String ENGLISH_ARTICLE_WITH_VIETNAMESE_NAMES =
            "More than 7,000 martyr remains sent on special flight to Hà Nội for DNA "
            + "identification. After the military transport aircraft arrived at Gia Lâm "
            + "Airport, the Air Defence-Air Force Service handed over the samples for road "
            + "transport to the Việt Nam Academy of Science and Technology, where DNA "
            + "testing will be conducted to identify the remains. Việt Nam needs an "
            + "integrated approach as urban flooding worsens in Đà Nẵng and Cần Thơ. "
            + "Flooding must therefore be managed through an integrated approach that "
            + "involves water resources, urban planning and disaster-risk governance, "
            + "with support from the people of Nghệ An and Đắk Lắk.";

    /**
     * Trang liệt kê tiếng Việt: nhiều tên riêng, gần như không có từ chức năng, và
     * phần lớn viết KHÔNG dấu. Tỷ lệ dấu thấp nhưng khác 0 — chốt cuối phải giữ
     * nhãn {@code vi} cho ca này.
     */
    private static final String VIETNAMESE_LISTING_PAGE =
            "Ha Noi Ho Chi Minh Da Nang Hai Phong Can Tho Nghe An Thanh Hoa Quang Ninh "
            + "Bac Ninh Nam Dinh Thai Binh Ninh Binh Ha Nam Hung Yen Hai Duong Bac Giang "
            + "Bac Kan Cao Bang Lang Son Tuyen Quang Ha Giang Lao Cai Yen Bai Phu Tho "
            + "Vinh Phuc Son La Dien Bien Lai Chau Hoa Binh Quang Tri Thua Thien Hue "
            + "Quang Nam Quang Ngai Binh Dinh Phu Yen Khanh Hoa Ninh Thuan Binh Thuan "
            + "Kon Tum Gia Lai Dak Lak Lâm Đồng";

    @Test
    void keepsVietnamese() {
        LanguageFilter filter = new LanguageFilter();
        assertEquals(LanguageFilter.VIETNAMESE, filter.detect("", VIETNAMESE_ARTICLE));
    }

    @Test
    void keepsEnglish() {
        LanguageFilter filter = new LanguageFilter();
        assertEquals(LanguageFilter.ENGLISH, filter.detect("", ENGLISH_ARTICLE));
    }

    @Test
    void rejectsChineseByScript() {
        LanguageFilter filter = new LanguageFilter();
        assertEquals("zh", filter.detect("", CHINESE_ARTICLE));
    }

    @Test
    void rejectsFrenchEvenThoughItIsLatinWithDiacritics() {
        LanguageFilter filter = new LanguageFilter();
        assertEquals(LanguageFilter.OTHER_LATIN, filter.detect("", FRENCH_ARTICLE));
    }

    @Test
    void toleratesAFewForeignCharactersInsideAVietnamesePage() {
        LanguageFilter filter = new LanguageFilter();
        String mixed = VIETNAMESE_ARTICLE + " (Trung Quốc: 越南)";
        assertEquals(LanguageFilter.VIETNAMESE, filter.detect("", mixed));
    }

    @Test
    void shortPagesFallBackToDeclaredLanguageAndAreKeptWhenUnknown() {
        LanguageFilter filter = new LanguageFilter();
        assertEquals(LanguageFilter.ENGLISH, filter.detect("en-US", "Home page"));
        assertEquals(LanguageFilter.VIETNAMESE, filter.detect("vi", "Trang chu"));
        assertEquals(LanguageFilter.UNDETERMINED, filter.detect("", "Trang chu"));
        assertEquals(LanguageFilter.UNDETERMINED, filter.detect("fr", "Accueil"));
    }

    @Test
    void vietnameseDiacriticsDecideEvenInVeryShortText() {
        LanguageFilter filter = new LanguageFilter();
        assertEquals(LanguageFilter.VIETNAMESE, filter.detect("", "Trang chủ"));
        assertEquals(LanguageFilter.VIETNAMESE, filter.detect("en", "Thể thao"));
    }

    @Test
    void vietnameseProperNounsDoNotMakeAnEnglishArticleVietnamese() {
        LanguageFilter filter = new LanguageFilter();
        assertEquals(LanguageFilter.ENGLISH,
                filter.detect("", ENGLISH_ARTICLE_WITH_VIETNAMESE_NAMES));
        // Kể cả khi <html lang> khai sai là "vi", nội dung vẫn phải thắng.
        assertEquals(LanguageFilter.ENGLISH,
                filter.detect("vi", ENGLISH_ARTICLE_WITH_VIETNAMESE_NAMES));
    }

    @Test
    void sparseDiacriticsStillDecideWhenNoFunctionWordEvidenceExists() {
        LanguageFilter filter = new LanguageFilter();
        assertEquals(LanguageFilter.VIETNAMESE, filter.detect("", VIETNAMESE_LISTING_PAGE));
    }

    @Test
    void contentBeatsAWrongHtmlLangAttribute() {
        LanguageFilter filter = new LanguageFilter();
        assertEquals(LanguageFilter.VIETNAMESE, filter.detect("en", VIETNAMESE_ARTICLE));
        assertEquals(LanguageFilter.ENGLISH, filter.detect("vi", ENGLISH_ARTICLE));
        assertEquals("zh", filter.detect("vi", CHINESE_ARTICLE));
    }

    @Test
    void acceptTagsTheDocumentAndCountsPerLanguage() {
        LanguageFilter filter = new LanguageFilter();

        assertTrue(filter.accept(docWith(VIETNAMESE_ARTICLE)));
        assertTrue(filter.accept(docWith(ENGLISH_ARTICLE)));
        assertFalse(filter.accept(docWith(CHINESE_ARTICLE)));
        assertFalse(filter.accept(docWith(FRENCH_ARTICLE)));

        assertEquals(1, filter.getAcceptedVietnameseCount());
        assertEquals(1, filter.getAcceptedEnglishCount());
        assertEquals(2, filter.getRejectedCount());
        assertEquals(1L, filter.getRejectedByLanguage().get("zh"));
        assertEquals(1L, filter.getRejectedByLanguage().get(LanguageFilter.OTHER_LATIN));
    }

    @Test
    void acceptWritesDetectedLanguageBackIntoTheDocument() {
        LanguageFilter filter = new LanguageFilter();
        WebDocument doc = docWith(ENGLISH_ARTICLE);
        doc.setLanguage("vi"); // <html lang> khai sai

        assertTrue(filter.accept(doc));
        assertEquals(LanguageFilter.ENGLISH, doc.getLanguage());
    }

    @Test
    void titleCountsAsEvidenceWhenTheBodyIsEmpty() {
        LanguageFilter filter = new LanguageFilter();
        WebDocument doc = new WebDocument();
        doc.setUrl("https://cn.example.vn/x");
        doc.setTitle(CHINESE_ARTICLE);
        doc.setBodyText("");

        assertFalse(filter.accept(doc));
        assertEquals("zh", doc.getLanguage());
    }

    @Test
    void normalizeLanguageTagKeepsOnlyThePrimarySubtag() {
        assertEquals("en", LanguageFilter.normalizeLanguageTag("en-US"));
        assertEquals("vi", LanguageFilter.normalizeLanguageTag("  VI  "));
        assertEquals("zh", LanguageFilter.normalizeLanguageTag("zh_CN"));
        assertEquals("", LanguageFilter.normalizeLanguageTag(null));
        assertEquals("", LanguageFilter.normalizeLanguageTag(""));
    }

    private static WebDocument docWith(String bodyText) {
        WebDocument doc = new WebDocument();
        doc.setUrl("https://example.vn/" + Math.abs(bodyText.hashCode()));
        doc.setTitle("");
        doc.setBodyText(bodyText);
        return doc;
    }
}
