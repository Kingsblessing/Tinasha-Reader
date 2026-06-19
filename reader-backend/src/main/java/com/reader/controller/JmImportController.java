package com.reader.controller;

import com.reader.common.BusinessException;
import com.reader.common.ErrorCode;
import com.reader.common.Result;
import com.reader.model.entity.Comic;
import com.reader.model.entity.Page;
import com.reader.model.entity.Source;
import com.reader.repository.ComicRepository;
import com.reader.repository.PageRepository;
import com.reader.repository.SourceRepository;
import com.reader.service.SettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/jm")
public class JmImportController {

    private static final Logger log = LoggerFactory.getLogger(JmImportController.class);

    // Project-relative paths
    private static final String TOOLS_DIR = System.getProperty("user.dir") + File.separator
            + "tools" + File.separator + "jm";
    private static final String JMCOMIC_PATH = TOOLS_DIR + File.separator + "jmcomic.exe";
    private static final String CONFIG_PATH = TOOLS_DIR + File.separator + "option.yml";

    // Default settings
    private static final String DEFAULT_DOWNLOAD_DIR = "D:\\World_Plan\\JMCrawler\\Download";
    private static final String DEFAULT_IMAGE_SUFFIX = ".jpg";

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp");

    private final ComicRepository comicRepository;
    private final PageRepository pageRepository;
    private final SourceRepository sourceRepository;
    private final SettingService settingService;

    public JmImportController(ComicRepository comicRepository,
                              PageRepository pageRepository,
                              SourceRepository sourceRepository,
                              SettingService settingService) {
        this.comicRepository = comicRepository;
        this.pageRepository = pageRepository;
        this.sourceRepository = sourceRepository;
        this.settingService = settingService;
    }

    /**
     * Import a JM comic by license plate number.
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importComic(@RequestBody Map<String, String> body) {
        String plateNumber = body.get("plateNumber");
        String downloadDir = body.get("downloadDir");

        if (plateNumber == null || plateNumber.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请输入车牌号");
        }

        // Clean plate number - extract digits only
        String cleanId = plateNumber.replaceAll("[^0-9]", "");
        if (cleanId.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "车牌号格式无效");
        }

        // Get download dir from param, setting, or default
        String targetDir = resolveDownloadDir(downloadDir);

        // Get image suffix from setting or default
        String imageSuffix = settingService.get("jm.image_suffix", DEFAULT_IMAGE_SUFFIX);

        log.info("[JM] Import plate={}, dir={}, suffix={}", cleanId, targetDir, imageSuffix);

        // Step 1: Generate option.yml dynamically
        generateConfig(targetDir, imageSuffix);

        // Step 2: Run jmcomic to download
        runJmDownload(cleanId);

        // Step 3: Find the downloaded comic folder
        File comicFolder = findDownloadedComic(targetDir, cleanId);
        if (comicFolder == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "下载完成但未找到漫画目录。请检查下载目录: " + targetDir);
        }

        log.info("[JM] Found comic folder: {}", comicFolder.getAbsolutePath());

        // Step 4: Check if already imported
        String absPath = comicFolder.getAbsolutePath();
        if (comicRepository.existsByFilePath(absPath)) {
            Comic existing = comicRepository.findByFilePath(absPath);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status", "already_exists");
            result.put("comicId", existing.getId());
            result.put("title", existing.getTitle());
            return Result.success(result);
        }

        // Step 5: Create or find a JM source
        Source jmSource = getOrCreateJmSource(targetDir);

        // Step 6: Import the comic
        Comic comic = importComicFromFolder(comicFolder, jmSource.getId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "imported");
        result.put("comicId", comic.getId());
        result.put("title", comic.getTitle());
        result.put("pageCount", comic.getPageCount());
        result.put("filePath", comic.getFilePath());
        return Result.success(result);
    }

    /**
     * Check jmcomic tool status.
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        boolean toolExists = new File(JMCOMIC_PATH).exists();
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("toolAvailable", toolExists);
        status.put("toolPath", JMCOMIC_PATH);
        status.put("configPath", CONFIG_PATH);
        status.put("downloadDir", settingService.get("jm.download_dir", DEFAULT_DOWNLOAD_DIR));
        status.put("imageSuffix", settingService.get("jm.image_suffix", DEFAULT_IMAGE_SUFFIX));
        return Result.success(status);
    }

    /**
     * Update JM settings.
     */
    @PutMapping("/settings")
    public Result<Void> updateSettings(@RequestBody Map<String, String> body) {
        if (body.containsKey("downloadDir")) {
            settingService.set("jm.download_dir", body.get("downloadDir"));
        }
        if (body.containsKey("imageSuffix")) {
            settingService.set("jm.image_suffix", body.get("imageSuffix"));
        }
        return Result.success();
    }

    private String resolveDownloadDir(String paramDir) {
        if (paramDir != null && !paramDir.isBlank()) {
            return paramDir;
        }
        return settingService.get("jm.download_dir", DEFAULT_DOWNLOAD_DIR);
    }

    /**
     * Generate option.yml dynamically based on current settings.
     */
    private void generateConfig(String downloadDir, String imageSuffix) {
        String config = "download:\n"
                + "  image:\n"
                + "    suffix: " + imageSuffix + "\n"
                + "dir_rule:\n"
                + "  base_dir: " + downloadDir.replace("\\", "/") + "\n";

        try {
            Files.writeString(Path.of(CONFIG_PATH), config, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("[JM] Generated config at {}", CONFIG_PATH);
        } catch (IOException e) {
            log.error("[JM] Failed to write config", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "写入配置文件失败: " + e.getMessage());
        }
    }

    private void runJmDownload(String plateId) {
        try {
            List<String> command = new ArrayList<>();
            command.add(JMCOMIC_PATH);
            command.add(plateId);
            command.add("--option=" + CONFIG_PATH);

            log.info("[JM] Running: {}", String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            pb.directory(new File(TOOLS_DIR));

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.info("[JM] {}", line);
                }
            }

            boolean finished = process.waitFor(300, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "下载超时（5分钟）");
            }

            if (process.exitValue() != 0) {
                log.error("[JM] Download failed (exit {}), output:\n{}", process.exitValue(), output);
                // Extract last meaningful error line
                String errorDetail = output.toString().lines()
                        .filter(l -> l.contains("错误") || l.contains("error") || l.contains("Error")
                                || l.contains("失败") || l.contains("fail") || l.contains("exception"))
                        .reduce((a, b) -> b)
                        .orElse(output.toString().lines().reduce((a, b) -> b).orElse("未知错误"));
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "下载失败: " + errorDetail);
            }

            log.info("[JM] Download completed successfully");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[JM] Error running jmcomic", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "执行 jmcomic 失败: " + e.getMessage());
        }
    }

    private File findDownloadedComic(String baseDir, String plateId) {
        File base = new File(baseDir);
        if (!base.exists() || !base.isDirectory()) {
            log.warn("[JM] Download dir does not exist: {}", baseDir);
            return null;
        }

        // Wait a moment for filesystem to settle
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        File[] dirs = base.listFiles(File::isDirectory);
        if (dirs == null || dirs.length == 0) return null;

        // Sort by last modified, newest first
        Arrays.sort(dirs, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

        // Check the most recently modified directories (within last 10 minutes)
        long cutoff = System.currentTimeMillis() - 10 * 60 * 1000;
        for (File dir : dirs) {
            if (dir.lastModified() < cutoff) continue;
            if (containsImages(dir)) {
                return dir;
            }
            // Check one level deeper (jmcomic may create subdirectories)
            File[] subDirs = dir.listFiles(File::isDirectory);
            if (subDirs != null) {
                for (File sub : subDirs) {
                    if (sub.lastModified() >= cutoff && containsImages(sub)) {
                        return sub;
                    }
                }
            }
        }

        // Fallback: check any directory with images
        for (File dir : dirs) {
            if (containsImages(dir)) return dir;
        }

        return null;
    }

    private boolean containsImages(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return false;
        for (File f : files) {
            if (f.isFile() && isImage(f.getName())) return true;
            if (f.isDirectory() && containsImages(f)) return true;
        }
        return false;
    }

    private boolean isImage(String name) {
        String lower = name.toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(lower::endsWith);
    }

    private Source getOrCreateJmSource(String downloadDir) {
        List<Source> sources = sourceRepository.findAll();
        for (Source s : sources) {
            if ("JM导入".equals(s.getName())) {
                return s;
            }
        }

        Source source = new Source();
        source.setName("JM导入");
        source.setPath(downloadDir);
        source.setType("FOLDER");
        source.setEnabled(true);
        source.setScanStatus("IDLE");
        source.setScanIntervalMin(0);
        source.setComicCount(0);

        long id = sourceRepository.save(source);
        source.setId(id);
        return source;
    }

    private Comic importComicFromFolder(File folder, long sourceId) {
        String title = folder.getName();
        // Clean up common JM naming patterns like [group] title [translation]
        title = title.replaceAll("^\\[.*?]\\s*", "").trim();
        if (title.isEmpty()) title = folder.getName();

        Comic comic = new Comic();
        comic.setSourceId(sourceId);
        comic.setTitle(title);
        comic.setFilePath(folder.getAbsolutePath());
        comic.setFileType("FOLDER");
        comic.setStatus("COMPLETED");
        comic.setReadCount(0);
        comic.setLastReadPage(0);
        comic.setFileSize(folderSize(folder));
        comic.setFileModifiedAt(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(folder.lastModified()),
                ZoneId.systemDefault()));

        long comicId = comicRepository.save(comic);
        comic.setId(comicId);

        // Collect and sort image files
        List<Path> imageFiles = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(folder.toPath())) {
            imageFiles = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> isImage(p.getFileName().toString()))
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("[JM] Error scanning folder", e);
        }

        if (!imageFiles.isEmpty()) {
            List<Page> pages = new ArrayList<>();
            for (int i = 0; i < imageFiles.size(); i++) {
                Path img = imageFiles.get(i);
                Page page = new Page();
                page.setComicId(comicId);
                page.setPageNumber(i + 1);
                page.setFileName(img.getFileName().toString());
                page.setFilePath(img.toAbsolutePath().toString());
                try { page.setFileSize(Files.size(img)); } catch (IOException ignored) {}
                pages.add(page);
            }
            pageRepository.saveAll(pages);
            comic.setPageCount(pages.size());
            comic.setCoverPath(imageFiles.get(0).toAbsolutePath().toString());
            comicRepository.update(comic);
        }

        // Update source comic count
        sourceRepository.incrementComicCount(sourceId, 1);

        log.info("[JM] Imported: {} ({} pages)", title, comic.getPageCount());
        return comic;
    }

    private long folderSize(File folder) {
        long size = 0;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) size += f.length();
                else if (f.isDirectory()) size += folderSize(f);
            }
        }
        return size;
    }
}
