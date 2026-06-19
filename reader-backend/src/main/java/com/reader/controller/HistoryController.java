package com.reader.controller;

import com.reader.common.PageResult;
import com.reader.common.Result;
import com.reader.model.entity.Comic;
import com.reader.model.entity.ReadingHistory;
import com.reader.model.vo.ReadingStatsVO;
import com.reader.service.HistoryService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public Result<PageResult<ReadingHistory>> findAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(historyService.findAll(page, size));
    }

    @PostMapping
    public Result<ReadingHistory> record(@RequestBody Map<String, Object> body) {
        long comicId = ((Number) body.get("comicId")).longValue();
        int startPage = ((Number) body.get("startPage")).intValue();
        int endPage = ((Number) body.get("endPage")).intValue();
        int durationSec = ((Number) body.get("durationSec")).intValue();
        return Result.success(historyService.record(comicId, startPage, endPage, durationSec));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable long id) {
        historyService.delete(id);
        return Result.success();
    }

    @DeleteMapping
    public Result<Integer> deleteBeforeDate(@RequestBody Map<String, String> body) {
        String beforeDateStr = body.get("beforeDate");
        LocalDateTime date = LocalDateTime.parse(beforeDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        int deleted = historyService.deleteBeforeDate(date);
        return Result.success(deleted);
    }

    @GetMapping("/stats")
    public Result<ReadingStatsVO> getStats() {
        return Result.success(historyService.getStats());
    }

    @GetMapping("/recent")
    public Result<List<Comic>> getRecentRead(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(historyService.getRecentRead(limit));
    }
}
