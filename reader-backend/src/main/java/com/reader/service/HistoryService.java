package com.reader.service;

import com.reader.common.BusinessException;
import com.reader.common.ErrorCode;
import com.reader.common.PageResult;
import com.reader.model.entity.Comic;
import com.reader.model.entity.ReadingHistory;
import com.reader.model.vo.ReadingStatsVO;
import com.reader.repository.ComicRepository;
import com.reader.repository.HistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class HistoryService {

    private static final Logger log = LoggerFactory.getLogger(HistoryService.class);

    private final HistoryRepository historyRepository;
    private final ComicRepository comicRepository;
    private final ComicService comicService;

    public HistoryService(HistoryRepository historyRepository,
                          ComicRepository comicRepository,
                          ComicService comicService) {
        this.historyRepository = historyRepository;
        this.comicRepository = comicRepository;
        this.comicService = comicService;
    }

    public PageResult<ReadingHistory> findAll(int page, int size) {
        return historyRepository.findAll(page, size);
    }

    public ReadingHistory record(long comicId, int startPage, int endPage, int durationSec) {
        // Validate comic exists
        if (comicRepository.findById(comicId) == null) {
            throw new BusinessException(ErrorCode.COMIC_NOT_FOUND, "Comic not found: " + comicId);
        }

        ReadingHistory history = new ReadingHistory();
        history.setComicId(comicId);
        history.setStartPage(startPage);
        history.setEndPage(endPage);
        history.setPagesRead(Math.abs(endPage - startPage) + 1);
        history.setDurationSec(durationSec);
        history.setStartedAt(LocalDateTime.now());
        history.setEndedAt(LocalDateTime.now());

        long id = historyRepository.save(history);
        history.setId(id);
        return history;
    }

    public void delete(long id) {
        historyRepository.deleteById(id);
    }

    public int deleteBeforeDate(LocalDateTime date) {
        return historyRepository.deleteBeforeDate(date);
    }

    public ReadingStatsVO getStats() {
        ReadingStatsVO stats = new ReadingStatsVO();

        try {
            Map<String, Object> totalStats = historyRepository.getTotalStats();
            log.info("[Stats] totalStats raw: {}", totalStats);

            Long totalDuration = totalStats.get("total_duration") != null
                    ? ((Number) totalStats.get("total_duration")).longValue() : 0L;
            Long totalComics = totalStats.get("total_comics") != null
                    ? ((Number) totalStats.get("total_comics")).longValue() : 0L;

            stats.setTotalComics(totalComics.intValue());
            stats.setTotalTimeSec(totalDuration.intValue());

            // Get daily stats for the past 30 days
            List<Map<String, Object>> dailyData = historyRepository.getDailyStats(30);
            log.info("[Stats] dailyData count: {}", dailyData.size());

            List<ReadingStatsVO.DailyStat> dailyStats = new ArrayList<>();
            int todayPages = 0;
            int weekPages = 0;
            int monthPages = 0;

            String today = LocalDateTime.now().toLocalDate().toString();

            for (Map<String, Object> row : dailyData) {
                String date = row.get("date") != null ? row.get("date").toString() : "";
                Number pagesNum = (Number) row.get("pages_read");
                int pages = pagesNum != null ? pagesNum.intValue() : 0;

                // MySQL DATE() may return java.sql.Date, toString gives "yyyy-MM-dd"
                String normalizedDate = date.length() > 10 ? date.substring(0, 10) : date;

                dailyStats.add(new ReadingStatsVO.DailyStat(normalizedDate, pages));
                monthPages += pages;

                if (normalizedDate.equals(today)) {
                    todayPages = pages;
                }
                if (dailyStats.size() <= 7) {
                    weekPages += pages;
                }
            }

            stats.setTodayPages(todayPages);
            stats.setWeekPages(weekPages);
            stats.setMonthPages(monthPages);
            stats.setDailyStats(dailyStats);

            log.info("[Stats] result: totalComics={}, today={}, week={}, month={}, totalTimeSec={}",
                    totalComics, todayPages, weekPages, monthPages, totalDuration);

        } catch (Exception e) {
            log.error("[Stats] error computing stats", e);
            stats.setTodayPages(0);
            stats.setWeekPages(0);
            stats.setMonthPages(0);
            stats.setTotalComics(0);
            stats.setTotalTimeSec(0);
            stats.setDailyStats(new ArrayList<>());
        }

        return stats;
    }

    public List<Comic> getRecentRead(int limit) {
        return comicService.getRecentRead(limit);
    }
}
