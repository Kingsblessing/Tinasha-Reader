package com.reader.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FileScanner {

    private static final Logger log = LoggerFactory.getLogger(FileScanner.class);

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp"
    );

    /**
     * Scans a root path for all comic sources (folders with images, archives, PDFs).
     */
    public List<ComicInfo> scanForComics(Path rootPath) {
        List<ComicInfo> comics = new ArrayList<>();
        File rootDir = rootPath.toFile();
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            log.warn("[Scan] Root path is not a directory: {}", rootPath);
            return comics;
        }

        File[] entries = rootDir.listFiles();
        if (entries == null) {
            log.warn("[Scan] Cannot list directory: {}", rootPath);
            return comics;
        }

        log.info("[Scan] Scanning {} entries in {}", entries.length, rootPath);

        for (File entry : entries) {
            if (entry.isDirectory()) {
                int imageCount = countImagesRecursive(entry);
                if (imageCount > 0) {
                    comics.add(new ComicInfo(
                            entry.toPath(), "FOLDER", entry.getName(), imageCount));
                    log.debug("[Scan] Found comic folder: {} ({} images)", entry.getName(), imageCount);
                }
            } else if (entry.isFile()) {
                String nameLower = entry.getName().toLowerCase();
                if (nameLower.endsWith(".cbz") || nameLower.endsWith(".zip")) {
                    comics.add(new ComicInfo(entry.toPath(), "CBZ", extractTitle(entry.getName()), -1));
                } else if (nameLower.endsWith(".cbr") || nameLower.endsWith(".rar")) {
                    comics.add(new ComicInfo(entry.toPath(), "CBR", extractTitle(entry.getName()), -1));
                } else if (nameLower.endsWith(".7z")) {
                    comics.add(new ComicInfo(entry.toPath(), "SEVEN_Z", extractTitle(entry.getName()), -1));
                } else if (nameLower.endsWith(".pdf")) {
                    comics.add(new ComicInfo(entry.toPath(), "PDF", extractTitle(entry.getName()), -1));
                }
            }
        }

        comics.sort(Comparator.comparing(c -> naturalSortKey(c.getTitle())));
        log.info("[Scan] Found {} comics total", comics.size());
        return comics;
    }

    /**
     * Counts images in a directory (non-recursive, for page counting).
     */
    public int countImagesInDirectory(Path dir) {
        File dirFile = dir.toFile();
        File[] files = dirFile.listFiles();
        if (files == null) return 0;
        int count = 0;
        for (File f : files) {
            if (f.isFile() && isImageFile(f.getName())) count++;
        }
        return count;
    }

    /**
     * Recursively counts images in a directory (for detection).
     */
    private int countImagesRecursive(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return 0;
        int count = 0;
        for (File f : files) {
            if (f.isFile() && isImageFile(f.getName())) {
                count++;
            } else if (f.isDirectory()) {
                count += countImagesRecursive(f);
            }
        }
        return count;
    }

    public boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(lower::endsWith);
    }

    public boolean isImageFile(Path file) {
        return isImageFile(file.getFileName().toString());
    }

    private String extractTitle(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private String naturalSortKey(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        Matcher matcher = Pattern.compile("\\d+|\\D+").matcher(input);
        while (matcher.find()) {
            String part = matcher.group();
            if (Character.isDigit(part.charAt(0))) {
                sb.append(String.format("%010d", Long.parseLong(part)));
            } else {
                sb.append(part.toLowerCase());
            }
        }
        return sb.toString();
    }

    public static class ComicInfo {
        private final Path path;
        private final String type;
        private final String title;
        private final int pageCount;

        public ComicInfo(Path path, String type, String title, int pageCount) {
            this.path = path;
            this.type = type;
            this.title = title;
            this.pageCount = pageCount;
        }

        public Path getPath() { return path; }
        public String getType() { return type; }
        public String getTitle() { return title; }
        public int getPageCount() { return pageCount; }
    }
}
