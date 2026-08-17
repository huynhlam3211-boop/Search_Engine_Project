package com.vnsearch.crawler.bus;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record ImageFound(
        String pageUrl,
        String host,
        String imageUrl,
        String altText,
        int declaredWidth,
        int declaredHeight,
        long sizeBytes,
        String contentHash) {

    public ImageFound {
        if (pageUrl == null || pageUrl.isBlank()) {
            throw new IllegalArgumentException("ImageFound.pageUrl must not be empty");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("ImageFound.imageUrl must not be empty");
        }
        altText = altText == null ? "" : altText;
    }

    public static ImageFound metadataOnly(String pageUrl, String host, String imageUrl,
                                           String altText, int width, int height) {
        return new ImageFound(pageUrl, host, imageUrl, altText, width, height, -1L, null);
    }

    @JsonIgnore
    public boolean isDownloaded() {
        return contentHash != null;
    }

    @JsonIgnore
    public boolean missingAlt() {
        return altText.isBlank();
    }

}