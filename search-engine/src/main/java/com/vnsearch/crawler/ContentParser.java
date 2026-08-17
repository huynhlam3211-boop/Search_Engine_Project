package com.vnsearch.crawler;

import com.vnsearch.model.WebDocument;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.Instant;

public class ContentParser {
    public WebDocument parse(String url, Documnet document) {
        WebDocument doc = new WebDocument();
        doc.setUrl(url);
        doc.setTitle(document.title());
        doc.setMetaDescription(extractMetaDescription(document));
        doc.setBodyText(extractBodyText(document));
        doc.setLanguage(extractDeclaredLanguage(document));
        doc.setCrawledAt(Instant.now());
        return doc;
    }

    private String extractedDeclaredLanguage(Document document) {

    }

    private String extractMetaDescription(Document document) {

    }

    private String extractedBodyText(Document document) {
        
    }
}