package com.vnsearch.crawler.bus;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @param pageUrl trang chứa ảnh 
 * @param host host của trang
 * @param imageUrl địa chỉ đã chuẩn hóa của ảnh
 * @param altText nội dung thuộc tích 
 * @param declaredWidth chiều rộng kai bao trong HTML
 * @param declaredHeight chiều cao khai báo trong HTML
 * @param sizeBytes kích thước thật
 * @param contentHash vân tay SHA-256 của nội dung
 */

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
            throw new IllegalArgumentException("ImageFound.pageUrl không được rỗng");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("ImageFound.imageUrl không được rỗng");
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