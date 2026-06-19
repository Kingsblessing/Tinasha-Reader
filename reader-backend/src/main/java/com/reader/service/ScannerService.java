package com.reader.service;

import com.reader.model.entity.Comic;
import com.reader.model.entity.Page;
import com.reader.model.entity.Source;
import com.reader.repository.ComicRepository;
import com.reader.repository.PageRepository;
import com.reader.repository.SourceRepository;
import com.reader.util.ComicParser;
import com.reader.util.ComicParserFactory;
import com.reader.util.FileScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScannerService {

    private static final Logger log = LoggerFactory.getLogger(ScannerService.class);

    private final FileScanner fileScanner;
    private final ComicParserFactory comicParserFactory;
    private final ComicRepository comicRepository;
    private final PageRepository pageRepository;
    private final SourceRepository sourceRepository;

    public ScannerService(FileScanner fileScanner,
                          ComicParserFactory comicParserFactory,
                          ComicRepository comicRepository,
                          PageRepository pageRepository,
                          SourceRepository sourceRepository) {
        this.fileScanner = fileScanner;
        this.comicParserFactory = comicParserFactory;
        this.comicRepository = comicRepository;
        this.pageRepository = pageRepository;
        this.sourceRepository = sourceRepository;
    }

    /**
     * Main scan logic for a source. Scans for comics, creates Comic + Page entities.
     */
    public void scanSource(Source source) {
        log.info("Starting scan for source: {} (path: {})", source.getName(), source.getPath());

        sourceRepository.updateScanStatus(source.getId(), "SCANNING");

        try {
            Path rootPath = Path.of(source.getPath());
            log.info("[Scan] Scanning path: {}, exists: {}", rootPath, java.nio.file.Files.isDirectory(rootPath));
            List<FileScanner.ComicInfo> comicInfos = fileScanner.scanForComics(rootPath);
            log.info("[Scan] Found {} comic candidates", comicInfos.size());

            int newComics = 0;
            for (FileScanner.ComicInfo info : comicInfos) {
                try {
                    String filePath = info.getPath().toAbsolutePath().toString();

                    // Skip if already exists in this source
                    if (comicRepository.existsBySourceAndFilePath(source.getId(), filePath)) {
                        log.debug("Comic already exists in source, skipping: {}", filePath);
                        continue;
                    }

                    // Create comic entity
                    Comic comic = new Comic();
                    comic.setSourceId(source.getId());
                    comic.setTitle(info.getTitle());
                    comic.setFilePath(filePath);
                    comic.setFileType(info.getType());
                    comic.setPageCount(info.getPageCount() > 0 ? info.getPageCount() : 0);
                    comic.setStatus("ONGOING");
                    comic.setReadCount(0);
                    comic.setLastReadPage(0);

                    // Try to set file metadata
                    File comicFile = info.getPath().toFile();
                    comic.setFileSize(comicFile.length());
                    comic.setFileModifiedAt(LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(comicFile.lastModified()),
                            java.time.ZoneId.systemDefault()));

                    long comicId = comicRepository.save(comic);
                    comic.setId(comicId);

                    // Extract pages using parser
                    ComicParser parser = comicParserFactory.getParser(comicFile);
                    if (parser != null) {
                        List<ComicParser.PageInfo> pageInfos = parser.extractPages(comicFile);
                        List<Page> pages = new ArrayList<>();
                        for (int i = 0; i < pageInfos.size(); i++) {
                            ComicParser.PageInfo pageInfo = pageInfos.get(i);
                            Page page = new Page();
                            page.setComicId(comicId);
                            page.setPageNumber(i + 1);
                            page.setFileName(pageInfo.getFileName());
                            page.setFilePath(pageInfo.getFilePath());
                            page.setWidth(pageInfo.getWidth());
                            page.setHeight(pageInfo.getHeight());
                            page.setFileSize(pageInfo.getFileSize());
                            pages.add(page);
                        }

                        if (!pages.isEmpty()) {
                            pageRepository.saveAll(pages);
                            comic.setPageCount(pages.size());
                            comicRepository.update(comic);
                        }

                        // Set cover path to first page
                        if (!pageInfos.isEmpty()) {
                            comic.setCoverPath(pageInfos.get(0).getFilePath());
                            comicRepository.update(comic);
                        }
                    }

                    newComics++;
                    log.debug("Added comic: {} ({} pages)", info.getTitle(), comic.getPageCount());

                } catch (Exception e) {
                    log.error("Error processing comic: {}", info.getTitle(), e);
                }
            }

            // Update source comic count
            int totalComics = comicRepository.countBySourceId(source.getId());
            source.setComicCount(totalComics);
            source.setScanStatus("IDLE");
            source.setLastScanAt(LocalDateTime.now());
            sourceRepository.update(source);
            sourceRepository.updateScanStatus(source.getId(), "IDLE");

            log.info("Scan completed for source: {}. Found {} new comics (total: {})",
                    source.getName(), newComics, totalComics);

        } catch (Exception e) {
            log.error("Scan failed for source: {}", source.getName(), e);
            sourceRepository.updateScanStatus(source.getId(), "ERROR");
        }
    }
}
