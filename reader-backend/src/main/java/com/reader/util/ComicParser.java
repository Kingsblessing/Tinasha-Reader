package com.reader.util;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Interface for parsing comic files and extracting page information.
 */
public interface ComicParser {
    boolean supports(File file);
    List<PageInfo> extractPages(File file) throws IOException;
    InputStream getPageStream(File file, String pagePath) throws IOException;
    byte[] getCoverData(File file) throws IOException;

    /**
     * Holds metadata about a single page in a comic.
     */
    class PageInfo {
        private String fileName;
        private String filePath;
        private int index;
        private Integer width;
        private Integer height;
        private Long fileSize;

        public PageInfo() {}

        public PageInfo(String fileName, String filePath, int index) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.index = index;
        }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        public Integer getWidth() { return width; }
        public void setWidth(Integer width) { this.width = width; }
        public Integer getHeight() { return height; }
        public void setHeight(Integer height) { this.height = height; }
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    }
}

/**
 * Parser for directory-based comics (folders containing image files).
 */
@Component
class FolderComicParser implements ComicParser {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp"
    );

    @Override
    public boolean supports(File file) {
        return file.isDirectory();
    }

    @Override
    public List<PageInfo> extractPages(File file) throws IOException {
        List<Path> imageFiles = new ArrayList<>();
        collectImages(file.toPath(), imageFiles);
        imageFiles.sort(this::naturalCompare);

        List<PageInfo> pages = new ArrayList<>();
        for (int i = 0; i < imageFiles.size(); i++) {
            Path imgPath = imageFiles.get(i);
            PageInfo info = new PageInfo(
                    imgPath.getFileName().toString(),
                    imgPath.toAbsolutePath().toString(),
                    i
            );
            try {
                info.setFileSize(Files.size(imgPath));
            } catch (IOException ignored) {}
            pages.add(info);
        }
        return pages;
    }

    @Override
    public InputStream getPageStream(File file, String pagePath) throws IOException {
        Path imagePath = file.toPath().resolve(pagePath);
        if (!Files.exists(imagePath)) {
            // Try absolute path
            imagePath = Path.of(pagePath);
        }
        if (!Files.exists(imagePath)) {
            throw new FileNotFoundException("Page not found: " + pagePath);
        }
        return Files.newInputStream(imagePath);
    }

    @Override
    public byte[] getCoverData(File file) throws IOException {
        List<Path> imageFiles = new ArrayList<>();
        collectImages(file.toPath(), imageFiles);
        imageFiles.sort(this::naturalCompare);

        if (imageFiles.isEmpty()) {
            return null;
        }
        return Files.readAllBytes(imageFiles.get(0));
    }

    private void collectImages(Path dir, List<Path> result) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    collectImages(entry, result);
                } else if (Files.isRegularFile(entry) && isImage(entry)) {
                    result.add(entry);
                }
            }
        }
    }

    private boolean isImage(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (name.endsWith(ext)) return true;
        }
        return false;
    }

    private int naturalCompare(Path a, Path b) {
        String sa = a.getFileName().toString();
        String sb = b.getFileName().toString();
        return naturalCompare(sa, sb);
    }

    private int naturalCompare(String a, String b) {
        Pattern pattern = Pattern.compile("(\\d+|\\D+)");
        Matcher ma = pattern.matcher(a);
        Matcher mb = pattern.matcher(b);
        while (ma.find() && mb.find()) {
            String pa = ma.group();
            String pb = mb.group();
            if (Character.isDigit(pa.charAt(0)) && Character.isDigit(pb.charAt(0))) {
                int cmp = Long.compare(Long.parseLong(pa), Long.parseLong(pb));
                if (cmp != 0) return cmp;
            } else {
                int cmp = pa.compareToIgnoreCase(pb);
                if (cmp != 0) return cmp;
            }
        }
        return a.compareToIgnoreCase(b);
    }
}

/**
 * Parser for CBZ/ZIP comic archives.
 */
@Component
class ZipComicParser implements ComicParser {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp"
    );

    @Override
    public boolean supports(File file) {
        if (!file.isFile()) return false;
        String name = file.getName().toLowerCase();
        return name.endsWith(".cbz") || name.endsWith(".zip");
    }

    @Override
    public List<PageInfo> extractPages(File file) throws IOException {
        List<PageInfo> pages = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(file)) {
            List<ZipEntry> imageEntries = new ArrayList<>();
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && isImage(entry.getName())) {
                    imageEntries.add(entry);
                }
            }
            imageEntries.sort((a, b) -> naturalCompare(a.getName(), b.getName()));

            for (int i = 0; i < imageEntries.size(); i++) {
                ZipEntry entry = imageEntries.get(i);
                String entryName = entry.getName();
                // Use just the filename part
                String fileName = entryName;
                int lastSlash = entryName.lastIndexOf('/');
                if (lastSlash >= 0) {
                    fileName = entryName.substring(lastSlash + 1);
                }
                PageInfo info = new PageInfo(fileName, entryName, i);
                info.setFileSize(entry.getSize());
                pages.add(info);
            }
        }
        return pages;
    }

    @Override
    public InputStream getPageStream(File file, String pagePath) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        ZipEntry entry = zipFile.getEntry(pagePath);
        if (entry == null) {
            zipFile.close();
            throw new FileNotFoundException("Entry not found in zip: " + pagePath);
        }
        // Wrap in a stream that closes the ZipFile when done
        InputStream entryStream = zipFile.getInputStream(entry);
        return new FilterInputStream(entryStream) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    zipFile.close();
                }
            }
        };
    }

    @Override
    public byte[] getCoverData(File file) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            List<ZipEntry> imageEntries = new ArrayList<>();
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && isImage(entry.getName())) {
                    imageEntries.add(entry);
                }
            }
            imageEntries.sort((a, b) -> naturalCompare(a.getName(), b.getName()));

            if (imageEntries.isEmpty()) {
                return null;
            }
            try (InputStream is = zipFile.getInputStream(imageEntries.get(0))) {
                return is.readAllBytes();
            }
        }
    }

    private boolean isImage(String name) {
        String lower = name.toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (lower.endsWith(ext)) return true;
        }
        return false;
    }

    private int naturalCompare(String a, String b) {
        Pattern pattern = Pattern.compile("(\\d+|\\D+)");
        Matcher ma = pattern.matcher(a);
        Matcher mb = pattern.matcher(b);
        while (ma.find() && mb.find()) {
            String pa = ma.group();
            String pb = mb.group();
            if (Character.isDigit(pa.charAt(0)) && Character.isDigit(pb.charAt(0))) {
                int cmp = Long.compare(Long.parseLong(pa), Long.parseLong(pb));
                if (cmp != 0) return cmp;
            } else {
                int cmp = pa.compareToIgnoreCase(pb);
                if (cmp != 0) return cmp;
            }
        }
        return a.compareToIgnoreCase(b);
    }
}

/**
 * Parser for PDF comic files. Returns page count metadata;
 * actual PDF rendering is handled at the image serving level.
 */
@Component
class PdfComicParser implements ComicParser {

    @Override
    public boolean supports(File file) {
        if (!file.isFile()) return false;
        return file.getName().toLowerCase().endsWith(".pdf");
    }

    @Override
    public List<PageInfo> extractPages(File file) throws IOException {
        // PDF page content extraction is handled at serving level.
        // We create placeholder PageInfo entries with page count.
        int pageCount = estimatePageCount(file);
        List<PageInfo> pages = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            PageInfo info = new PageInfo("page_" + (i + 1) + ".pdf", "page:" + i, i);
            pages.add(info);
        }
        return pages;
    }

    @Override
    public InputStream getPageStream(File file, String pagePath) throws IOException {
        // PDF pages are served as rendered images at the controller level
        throw new UnsupportedOperationException("PDF page rendering should be handled at the image serving level");
    }

    @Override
    public byte[] getCoverData(File file) throws IOException {
        // PDF cover rendering would require a PDF library; return null for now
        return null;
    }

    /**
     * Estimates page count by scanning for the /Type /Page entries in the PDF.
     * This is a rough heuristic and may not work for all PDFs.
     */
    private int estimatePageCount(File file) {
        int count = 0;
        try (InputStream is = new FileInputStream(file)) {
            byte[] data = is.readAllBytes();
            String content = new String(data, java.nio.charset.StandardCharsets.ISO_8859_1);
            // Count "/Type /Page" occurrences that are NOT "/Type /Pages"
            String search = "/Type /Page";
            int idx = 0;
            while ((idx = content.indexOf(search, idx)) != -1) {
                int afterIdx = idx + search.length();
                if (afterIdx < content.length() && content.charAt(afterIdx) != 's') {
                    count++;
                }
                idx = afterIdx;
            }
        } catch (IOException ignored) {
            count = 1; // fallback
        }
        return Math.max(count, 1);
    }
}

