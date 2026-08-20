package com.vnsearch.crawler.modular;

import com.vnsearch.crawler.bus.ImageFound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageStorageTest {

    private static ImageFound image(String pageUrl, String imageUrl, String alt) {
        return ImageFound.metadataOnly(pageUrl, "a.vn", imageUrl, alt, 800, 600);
    }

    @Test
    void derivesImagePathFromCorpusPath() {
        assertEquals("data/crawled-documents.images.json",
                ImageStorage.pathFor("data/crawled-documents.json"));
       
        assertEquals("data/corpus.images.json", ImageStorage.pathFor("data/corpus"));
    }

    @Test
    void writesAndReadsBackEveryField(@TempDir Path dir) throws IOException {
        ImageFound original = new ImageFound(
                "https://a.vn/bai", "a.vn", "https://a.vn/1.jpg", "mo ta", 800, 600, 1234L, "abc123");
        String path = dir.resolve("corpus.images.json").toString();

        ImageStorage.saveToJson(List.of(original), path);
        List<ImageFound> back = ImageStorage.loadFromJson(path);

        assertEquals(1, back.size());
        assertEquals(original, back.get(0));
        assertTrue(back.get(0).isDownloaded());
        assertFalse(back.get(0).missingAlt());
    }

    @Test
    void keepsMetadataOnlyRecordsIntact(@TempDir Path dir) throws IOException {
        ImageFound original = image("https://a.vn/bai", "https://a.vn/1.jpg", "");
        String path = dir.resolve("corpus.images.json").toString();

        ImageStorage.saveToJson(List.of(original), path);
        ImageFound back = ImageStorage.loadFromJson(path).get(0);

        assertEquals(original, back);
        // Hai gia tri suy ra phai giu nguyen y nghia sau mot vong ghi/doc: anh
        // chua tai noi dung, va alt rong nghia la anh trang tri.
        assertFalse(back.isDownloaded());
        assertTrue(back.missingAlt());
    }

    @Test
    void writesFileEvenWhenThereAreNoImages(@TempDir Path dir) throws IOException {
        String path = dir.resolve("corpus.images.json").toString();

        ImageStorage.saveToJson(List.of(), path);

        assertTrue(Files.exists(Path.of(path)));
        assertEquals(List.of(), ImageStorage.loadFromJson(path));
    }

    @Test
    void createsMissingParentDirectories(@TempDir Path dir) throws IOException {
        String path = dir.resolve("chua-co").resolve("corpus.images.json").toString();

        ImageStorage.saveToJson(List.of(image("https://a.vn/b", "https://a.vn/1.jpg", "x")), path);

        assertEquals(1, ImageStorage.loadFromJson(path).size());
    }

    @Test
    void leavesNoTempFileBehind(@TempDir Path dir) throws IOException {
        String path = dir.resolve("corpus.images.json").toString();

        ImageStorage.saveToJson(List.of(image("https://a.vn/b", "https://a.vn/1.jpg", "x")), path);

        assertFalse(Files.exists(Path.of(path + ".tmp")));
    }

    @Test
    void overwritesInsteadOfAppending(@TempDir Path dir) throws IOException {
        String path = dir.resolve("corpus.images.json").toString();

        ImageStorage.saveToJson(List.of(
                image("https://a.vn/b", "https://a.vn/1.jpg", "x"),
                image("https://a.vn/b", "https://a.vn/2.jpg", "y")), path);
        ImageStorage.saveToJson(List.of(image("https://a.vn/b", "https://a.vn/3.jpg", "z")), path);

        List<ImageFound> back = ImageStorage.loadFromJson(path);
        assertEquals(1, back.size());
        assertEquals("https://a.vn/3.jpg", back.get(0).imageUrl());
    }

    @Test
    void writesOneFieldPerLineForTheStatsScript(@TempDir Path dir) throws IOException {
        String path = dir.resolve("corpus.images.json").toString();

        ImageStorage.saveToJson(List.of(image("https://a.vn/b", "https://a.vn/1.jpg", "mo ta")), path);

        List<String> trimmed = Files.readAllLines(Path.of(path)).stream()
                .map(String::trim)
                .filter(line -> line.startsWith("\""))
                .toList();
        assertLinesMatch(List.of(
                "\"pageUrl\" : .*",
                "\"host\" : .*",
                "\"imageUrl\" : .*",
                "\"altText\" : .*",
                "\"declaredWidth\" : .*",
                "\"declaredHeight\" : .*",
                "\"sizeBytes\" : .*",
                "\"contentHash\" : .*"), trimmed);
    }

    @Test
    void loadQuietlyNeverThrows(@TempDir Path dir) throws IOException {
        assertEquals(List.of(), ImageStorage.loadQuietly(dir.resolve("khong-co.json").toString()));
        assertEquals(List.of(), ImageStorage.loadQuietly(null));

        Path broken = dir.resolve("hong.images.json");
        Files.writeString(broken, "{ day khong phai JSON hop le");
        assertEquals(List.of(), ImageStorage.loadQuietly(broken.toString()));
    }
}
