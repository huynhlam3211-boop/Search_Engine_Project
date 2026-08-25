package com.vnsearch.crawler;

import com.vnsearch.model.WebDocument;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.Instant;

public class ContentParser {

    public WebDocument parse(String url, Document document) {
        WebDocument doc = new WebDocument();
        doc.setUrl(url);
        doc.setTitle(document.title());
        doc.setMetaDescription(extractMetaDescription(document));
        doc.setBodyText(extractBodyText(document));
        doc.setLanguage(extractDeclaredLanguage(document));
        doc.setCrawledAt(Instant.now());
        return doc;
    }

    private String extractDeclaredLanguage(Document document) {
        Element html = document.selectFirst("html");
        String declared = html != null ? html.attr("lang") : "";
        if (declared.isBlank()) {
            Element meta = document.selectFirst("meta[http-equiv=content-language]");
            if (meta == null) {
                meta = document.selectFirst("meta[property=og:locale]");
            }
            declared = meta != null ? meta.attr("content") : "";
        }
        return LanguageFilter.normalizeLanguageTag(declared);
    }

    private String extractMetaDescription(Document document) {
        Element meta = document.selectFirst("meta[name=description]");
        if (meta == null) {
            meta = document.selectFirst("meta[property=og:description]");
        }
        return meta != null ? meta.attr("content").trim() : "";
    }

    private String extractBodyText(Document document) {
        Document clone = document.clone();
        clone.select("script, style, noscript, nav, footer, header, iframe, svg").remove();
        return clone.body() != null ? clone.body().text().trim() : "";
    }
}


/**
 * HTML ĐẦU VÀO                                    KẾT QUẢ MONG ĐỢI
   ────────────────────────────────────────        ────────────────────────
   <title>Bài A</title>                            title = "Bài A"
   <meta name=description content=" X ">           metaDescription = "X"  (đã trim)
   chỉ có <meta property=og:description>           dùng og:description
   không có meta nào                               ""  (không null)
   <html lang="vi">                                language = "vi"
   <html lang="en-US">                             normalizeLanguageTag("en-US")
   không có lang, có og:locale=vi_VN               dùng og:locale
   <body>A<script>var x=1</script>B</body>         bodyText = "A B"  (không có "var x=1")
   <body><nav>Menu</nav>Nội dung</body>            bodyText = "Nội dung"
   không có <body>                                 bodyText = ""  (không ném)
 */