package com.reader.repository;

import com.reader.common.PageResult;
import com.reader.model.entity.ReadingHistory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class HistoryRepository {

    private final NamedParameterJdbcTemplate namedJdbc;
    private final RowMapper<ReadingHistory> rowMapper = new BeanPropertyRowMapper<>(ReadingHistory.class);

    public HistoryRepository(NamedParameterJdbcTemplate namedJdbc) {
        this.namedJdbc = namedJdbc;
    }

    public PageResult<ReadingHistory> findAll(int page, int size) {
        long totalElements = namedJdbc.getJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM reading_history", Long.class);
        int offset = (page - 1) * size;
        List<ReadingHistory> content = namedJdbc.getJdbcTemplate().query(
                "SELECT * FROM reading_history ORDER BY started_at DESC LIMIT ? OFFSET ?",
                rowMapper, size, offset);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResult<>(content, totalElements, totalPages, page, size);
    }

    public List<ReadingHistory> findByComicId(long comicId) {
        return namedJdbc.getJdbcTemplate().query(
                "SELECT * FROM reading_history WHERE comic_id = ? ORDER BY started_at DESC",
                rowMapper, comicId);
    }

    public long save(ReadingHistory history) {
        String sql = "INSERT INTO reading_history (comic_id, start_page, end_page, pages_read, "
                + "duration_sec, started_at, ended_at) "
                + "VALUES (:comicId, :startPage, :endPage, :pagesRead, "
                + ":durationSec, :startedAt, :endedAt)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("comicId", history.getComicId())
                .addValue("startPage", history.getStartPage())
                .addValue("endPage", history.getEndPage())
                .addValue("pagesRead", history.getPagesRead())
                .addValue("durationSec", history.getDurationSec())
                .addValue("startedAt", history.getStartedAt())
                .addValue("endedAt", history.getEndedAt());
        namedJdbc.update(sql, params, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void deleteById(long id) {
        namedJdbc.getJdbcTemplate().update("DELETE FROM reading_history WHERE id = ?", id);
    }

    public int deleteBeforeDate(LocalDateTime date) {
        return namedJdbc.getJdbcTemplate().update(
                "DELETE FROM reading_history WHERE started_at < ?", date);
    }

    public List<Map<String, Object>> getDailyStats(int days) {
        return namedJdbc.getJdbcTemplate().queryForList(
                "SELECT DATE(started_at) AS date, "
                + "SUM(pages_read) AS pages_read, "
                + "SUM(duration_sec) AS duration_sec "
                + "FROM reading_history "
                + "WHERE started_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY) "
                + "GROUP BY DATE(started_at) "
                + "ORDER BY date DESC", days);
    }

    public Map<String, Object> getTotalStats() {
        return namedJdbc.getJdbcTemplate().queryForMap(
                "SELECT COUNT(DISTINCT comic_id) AS total_comics, "
                + "SUM(pages_read) AS total_pages, "
                + "SUM(duration_sec) AS total_duration "
                + "FROM reading_history");
    }

    public long count() {
        Long count = namedJdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM reading_history", Long.class);
        return count != null ? count : 0;
    }
}
