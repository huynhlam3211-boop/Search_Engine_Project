package com.vnsearch.crawler.bus;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
*
* @param pageUrl     trang chứa ảnh
* @param host        host của trang — khoá phân hoạch
* @param imageUrl    địa chỉ tuyệt đối, đã chuẩn hoá, của ảnh
* @param altText     nội dung thuộc tính {@code alt}; rỗng nếu trang không có
* @param declaredWidth  chiều rộng khai báo trong HTML, {@code -1} nếu không khai báo
* @param declaredHeight chiều cao khai báo trong HTML, {@code -1} nếu không khai báo
* @param sizeBytes   kích thước thật, {@code -1} khi chưa tải nội dung
* @param contentHash vân tay SHA-256 của nội dung, {@code null} khi chưa tải
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