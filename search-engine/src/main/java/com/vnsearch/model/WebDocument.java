package com.vnsearch.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class WebDocument {
    private int docId;
    private String url;
    private String title;
    private String metaDescription;
    private String bodyText;
    private List<String> outlinks = new ArrayList<>();
    private Instant crawledAt;

    private String language = "";

    public WebDocument() {
    }

    public WebDocument(int docId, String url, String title, String metaDescription,
                        String bodyText, List<String> outlinks, Instant crawledAt) {
        this.docId = docId;
        this.url = url;
        this.title = title;
        this.metaDescription = metaDescription;
        this.bodyText = bodyText;
        this.outlinks = outlinks != null ? outlinks : new ArrayList<>();
        this.crawledAt = crawledAt;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public List<String> getOutlinks() {
        return outlinks;
    }

    public void setOutlinks(List<String> outlinks) {
        this.outlinks = outlinks;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language == null ? "" : language;
    }

    public Instant getCrawledAt() {
        return crawledAt;
    }

    public void setCrawledAt(Instant crawledAt) {
        this.crawledAt = crawledAt;
    }

    public WebDocument withoutBodyText() {
        WebDocument copy = new WebDocument(docId, url, title, metaDescription, null,
                outlinks, crawledAt);
        copy.setLanguage(language);
        return copy;
    }

    @Override
    public String toString() {
        return "WebDocument{docId=" + docId + ", url='" + url + "', title='" + title
                + "', outlinks=" + outlinks.size() + "}";
    }


}
