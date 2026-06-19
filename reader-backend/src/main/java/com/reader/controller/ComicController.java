package com.reader.controller;

import com.reader.common.PageResult;
import com.reader.common.Result;
import com.reader.model.dto.ComicUpdateDTO;
import com.reader.model.entity.Comic;
import com.reader.model.entity.Page;
import com.reader.model.vo.ComicDetailVO;
import com.reader.service.ComicService;
import com.reader.util.ImageUtil;
import com.reader.util.ComicParser;
import com.reader.util.ComicParserFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import java.io.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/comics")
public class ComicController {

    private final ComicService comicService;
    private final ImageUtil imageUtil;
    private final ComicParserFactory comicParserFactory;

    public ComicController(ComicService comicService,
                           ImageUtil imageUtil,
                           ComicParserFactory comicParserFactory) {
        this.comicService = comicService;
        this.imageUtil = imageUtil;
        this.comicParserFactory = comicParserFactory;
    }

    @GetMapping
    public Result<PageResult<Comic>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long sourceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tagIds,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Long> tagIdList = null;
        if (tagIds != null && !tagIds.isEmpty()) {
            tagIdList = new ArrayList<>();
            for (String s : tagIds.split(",")) {
                try { tagIdList.add(Long.parseLong(s.trim())); } catch (NumberFormatException ignored) {}
            }
        }
        return Result.success(comicService.findAll(search, sourceId, status, tagIdList, sort, page, size));
    }

    @GetMapping("/{id}")
    public Result<ComicDetailVO> findById(@PathVariable long id) {
        return Result.success(comicService.findByIdWithDetails(id));
    }

    @PutMapping("/{id}")
    public Result<Comic> update(@PathVariable long id, @RequestBody ComicUpdateDTO dto) {
        return Result.success(comicService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable long id) {
        comicService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/rating")
    public Result<Comic> updateRating(@PathVariable long id, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal rating = body.get("rating");
        return Result.success(comicService.updateRating(id, rating));
    }

    @GetMapping("/{id}/pages")
    public Result<List<Page>> getPages(@PathVariable long id) {
        return Result.success(comicService.getPages(id));
    }

    @GetMapping("/{id}/pages/{pageNum}")
    public void getPageImage(@PathVariable long id, @PathVariable int pageNum,
                             HttpServletResponse response) throws IOException {
        Page page = comicService.getPage(id, pageNum);
        Comic comic = comicService.findById(id);

        File comicFile = new File(comic.getFilePath());
        ComicParser parser = comicParserFactory.getParser(comicFile);

        if (parser == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try (InputStream is = parser.getPageStream(comicFile, page.getFilePath())) {
            String contentType = imageUtil.getContentType(page.getFileName());
            response.setContentType(contentType);
            response.setHeader("Cache-Control", "max-age=86400");
            is.transferTo(response.getOutputStream());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/cover")
    public void getCover(@PathVariable long id,
                         HttpServletResponse response) throws IOException {
        Comic comic = comicService.findById(id);

        File comicFile = new File(comic.getFilePath());
        ComicParser parser = comicParserFactory.getParser(comicFile);

        if (parser == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            byte[] coverData = parser.getCoverData(comicFile);
            if (coverData == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType("image/jpeg");
            response.setHeader("Cache-Control", "max-age=86400");
            response.getOutputStream().write(coverData);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/progress")
    public Result<Void> updateProgress(@PathVariable long id, @RequestBody Map<String, Integer> body) {
        int page = body.get("page");
        comicService.updateReadProgress(id, page);
        return Result.success();
    }

    @GetMapping("/recent")
    public Result<List<Comic>> getRecentRead(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(comicService.getRecentRead(limit));
    }
}
