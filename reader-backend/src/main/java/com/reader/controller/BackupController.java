package com.reader.controller;

import com.reader.common.Result;
import com.reader.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/backup")
public class BackupController {

    private static final Logger log = LoggerFactory.getLogger(BackupController.class);

    private final NamedParameterJdbcTemplate namedJdbc;
    private final SourceRepository sourceRepository;
    private final ComicRepository comicRepository;
    private final PageRepository pageRepository;
    private final TagRepository tagRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReviewRepository reviewRepository;
    private final HistoryRepository historyRepository;
    private final SettingRepository settingRepository;

    public BackupController(NamedParameterJdbcTemplate namedJdbc,
                            SourceRepository sourceRepository,
                            ComicRepository comicRepository,
                            PageRepository pageRepository,
                            TagRepository tagRepository,
                            FavoriteRepository favoriteRepository,
                            ReviewRepository reviewRepository,
                            HistoryRepository historyRepository,
                            SettingRepository settingRepository) {
        this.namedJdbc = namedJdbc;
        this.sourceRepository = sourceRepository;
        this.comicRepository = comicRepository;
        this.pageRepository = pageRepository;
        this.tagRepository = tagRepository;
        this.favoriteRepository = favoriteRepository;
        this.reviewRepository = reviewRepository;
        this.historyRepository = historyRepository;
        this.settingRepository = settingRepository;
    }

    /**
     * GET /api/v1/backup/export - Export all data as JSON.
     * Returns a JSON object with table names as keys and lists of row maps as values.
     */
    @GetMapping("/export")
    public Result<Map<String, Object>> export() {
        try {
            Map<String, Object> backup = new LinkedHashMap<>();
            backup.put("source", queryAll("source"));
            backup.put("comic", queryAll("comic"));
            backup.put("page", queryAll("page"));
            backup.put("tag", queryAll("tag"));
            backup.put("comic_tag", queryAll("comic_tag"));
            backup.put("favorite", queryAll("favorite"));
            backup.put("review", queryAll("review"));
            backup.put("reading_history", queryAll("reading_history"));
            backup.put("setting", queryAll("setting"));

            log.info("Backup exported successfully");
            return Result.success(backup);
        } catch (Exception e) {
            log.error("Failed to export backup", e);
            return Result.error("Failed to export backup: " + e.getMessage());
        }
    }

    /**
     * POST /api/v1/backup/import - Import data from JSON backup.
     * Clears existing data and inserts all records from the backup.
     */
    @PostMapping("/import")
    @Transactional
    public Result<Map<String, Object>> importBackup(@RequestBody Map<String, List<Map<String, Object>>> backup) {
        try {
            // Delete in reverse dependency order
            namedJdbc.getJdbcTemplate().update("DELETE FROM reading_history");
            namedJdbc.getJdbcTemplate().update("DELETE FROM review");
            namedJdbc.getJdbcTemplate().update("DELETE FROM favorite");
            namedJdbc.getJdbcTemplate().update("DELETE FROM comic_tag");
            namedJdbc.getJdbcTemplate().update("DELETE FROM page");
            namedJdbc.getJdbcTemplate().update("DELETE FROM comic");
            namedJdbc.getJdbcTemplate().update("DELETE FROM tag");
            namedJdbc.getJdbcTemplate().update("DELETE FROM source");
            namedJdbc.getJdbcTemplate().update("DELETE FROM `setting`");

            // Insert in dependency order
            Map<String, Object> importResult = new LinkedHashMap<>();
            importResult.put("source", insertAll("source", backup.get("source")));
            importResult.put("tag", insertAll("tag", backup.get("tag")));
            importResult.put("comic", insertAll("comic", backup.get("comic")));
            importResult.put("page", insertAll("page", backup.get("page")));
            importResult.put("comic_tag", insertAll("comic_tag", backup.get("comic_tag")));
            importResult.put("favorite", insertAll("favorite", backup.get("favorite")));
            importResult.put("review", insertAll("review", backup.get("review")));
            importResult.put("reading_history", insertAll("reading_history", backup.get("reading_history")));
            importResult.put("setting", insertAll("setting", backup.get("setting")));

            log.info("Backup imported successfully");
            return Result.success(importResult);
        } catch (Exception e) {
            log.error("Failed to import backup", e);
            throw e; // Let @Transactional rollback
        }
    }

    /**
     * GET /api/v1/backup/info - Get backup info with record counts per table.
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        try {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("source", sourceRepository.count());
            info.put("comic", comicRepository.count());
            info.put("page", pageRepository.count());
            info.put("tag", tagRepository.count());
            info.put("comic_tag", countTable("comic_tag"));
            info.put("favorite", favoriteRepository.count());
            info.put("review", reviewRepository.count());
            info.put("reading_history", historyRepository.count());
            info.put("setting", settingRepository.count());
            return Result.success(info);
        } catch (Exception e) {
            log.error("Failed to get backup info", e);
            return Result.error("Failed to get backup info: " + e.getMessage());
        }
    }

    /**
     * Query all rows from a table, returning a list of column-value maps.
     */
    private List<Map<String, Object>> queryAll(String tableName) {
        return namedJdbc.queryForList("SELECT * FROM " + tableName, new MapSqlParameterSource());
    }

    /**
     * Insert all rows into a table from a list of column-value maps.
     * Returns the number of rows inserted.
     */
    private int insertAll(String tableName, List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Map<String, Object> row : rows) {
            if (row == null || row.isEmpty()) {
                continue;
            }
            // Build dynamic INSERT statement
            List<String> columns = new ArrayList<>(row.keySet());
            String columnList = String.join(", ", columns);
            String placeholderList = columns.stream()
                    .map(c -> ":" + c)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            String sql = "INSERT INTO " + tableName + " (" + columnList + ") VALUES (" + placeholderList + ")";

            MapSqlParameterSource params = new MapSqlParameterSource();
            for (String column : columns) {
                params.addValue(column, convertValue(column, row.get(column)));
            }
            namedJdbc.update(sql, params);
            count++;
        }
        return count;
    }

    /**
     * Convert a deserialized JSON value to the appropriate Java type for JDBC insertion.
     * Handles LocalDateTime columns (identified by _at suffix) and numeric types.
     */
    private Object convertValue(String columnName, Object value) {
        if (value == null) {
            return null;
        }
        // Convert date strings to LocalDateTime
        if (value instanceof String str && isDateColumn(columnName)) {
            try {
                return LocalDateTime.parse(str);
            } catch (DateTimeParseException e) {
                // Try with 'T' separator replacement for formats like "2024-01-01 00:00:00"
                try {
                    return LocalDateTime.parse(str.replace(" ", "T"));
                } catch (DateTimeParseException e2) {
                    log.warn("Failed to parse date value '{}' for column '{}', storing as null", str, columnName);
                    return null;
                }
            }
        }
        // Convert Integer to Long for BIGINT columns
        if (value instanceof Integer intVal && isBigIntColumn(columnName)) {
            return intVal.longValue();
        }
        return value;
    }

    /**
     * Check if a column name represents a datetime column.
     */
    private boolean isDateColumn(String columnName) {
        return columnName.endsWith("_at") || columnName.equals("started_at") || columnName.equals("ended_at");
    }

    /**
     * Check if a column name represents a BIGINT column that needs Long conversion.
     */
    private boolean isBigIntColumn(String columnName) {
        return columnName.equals("id") || columnName.equals("source_id") || columnName.equals("comic_id")
                || columnName.equals("tag_id") || columnName.equals("file_size")
                || columnName.equals("parent_id");
    }

    /**
     * Count rows in a table using direct SQL.
     */
    private long countTable(String tableName) {
        Long count = namedJdbc.getJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM " + tableName, Long.class);
        return count != null ? count : 0;
    }
}
