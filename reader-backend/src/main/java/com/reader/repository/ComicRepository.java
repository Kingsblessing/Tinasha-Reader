package com.reader.repository;

import com.reader.common.PageResult;
import com.reader.model.entity.Comic;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ComicRepository {

    private final NamedParameterJdbcTemplate namedJdbc;
    private final RowMapper<Comic> rowMapper = new BeanPropertyRowMapper<>(Comic.class);

    public ComicRepository(NamedParameterJdbcTemplate namedJdbc) {
        this.namedJdbc = namedJdbc;
    }

    public PageResult<Comic> findAll(String search, Long sourceId, String status,
                                      List<Long> tagIds, String sort, int page, int size) {
        boolean hasTagFilter = tagIds != null && !tagIds.isEmpty();

        // Build base FROM clause
        String baseFrom = hasTagFilter
                ? "comic c INNER JOIN comic_tag ct ON c.id = ct.comic_id"
                : "comic c";

        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (hasTagFilter) {
            // Filter comics that have ANY of the specified tags
            String placeholders = String.join(",", tagIds.stream().map(t -> "?").toArray(String[]::new));
            whereClause.append(" AND ct.tag_id IN (").append(placeholders).append(")");
            params.addAll(tagIds);
        }
        if (search != null && !search.isEmpty()) {
            whereClause.append(" AND c.title LIKE ?");
            params.add("%" + search + "%");
        }
        if (sourceId != null) {
            whereClause.append(" AND c.source_id = ?");
            params.add(sourceId);
        }
        if (status != null && !status.isEmpty()) {
            whereClause.append(" AND c.status = ?");
            params.add(status);
        }

        // Use DISTINCT when joining with comic_tag to avoid duplicates
        String selectPrefix = hasTagFilter ? "SELECT DISTINCT c.* FROM " : "SELECT c.* FROM ";

        String countSql = "SELECT COUNT(*) FROM " + baseFrom + whereClause;
        long totalElements = namedJdbc.getJdbcTemplate().queryForObject(countSql, Long.class, params.toArray());

        String orderClause;
        if (sort == null || sort.isEmpty()) {
            orderClause = " ORDER BY c.created_at DESC";
        } else {
            switch (sort) {
                case "title": orderClause = " ORDER BY c.title ASC"; break;
                case "rating": orderClause = " ORDER BY c.rating DESC"; break;
                case "createdAt": orderClause = " ORDER BY c.created_at DESC"; break;
                case "lastReadAt": orderClause = " ORDER BY c.last_read_at DESC"; break;
                default: orderClause = " ORDER BY c.created_at DESC"; break;
            }
        }

        int offset = (page - 1) * size;
        String querySql = selectPrefix + baseFrom + whereClause + orderClause + " LIMIT ? OFFSET ?";
        params.add(size);
        params.add(offset);

        List<Comic> content = namedJdbc.getJdbcTemplate().query(querySql, rowMapper, params.toArray());
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResult<>(content, totalElements, totalPages, page, size);
    }

    public Comic findById(long id) {
        try {
            return namedJdbc.getJdbcTemplate().queryForObject("SELECT * FROM comic WHERE id = ?", rowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Comic> findBySourceId(long sourceId) {
        return namedJdbc.getJdbcTemplate().query("SELECT * FROM comic WHERE source_id = ? ORDER BY title ASC", rowMapper, sourceId);
    }

    public long save(Comic comic) {
        String sql = "INSERT INTO comic (source_id, title, author, publisher, description, cover_path, "
                + "file_path, file_type, page_count, language, status, rating, read_count, "
                + "last_read_at, last_read_page, file_size, file_modified_at, created_at, updated_at) "
                + "VALUES (:sourceId, :title, :author, :publisher, :description, :coverPath, "
                + ":filePath, :fileType, :pageCount, :language, :status, :rating, :readCount, "
                + ":lastReadAt, :lastReadPage, :fileSize, :fileModifiedAt, :createdAt, :updatedAt)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("sourceId", comic.getSourceId())
                .addValue("title", comic.getTitle())
                .addValue("author", comic.getAuthor())
                .addValue("publisher", comic.getPublisher())
                .addValue("description", comic.getDescription())
                .addValue("coverPath", comic.getCoverPath())
                .addValue("filePath", comic.getFilePath())
                .addValue("fileType", comic.getFileType())
                .addValue("pageCount", comic.getPageCount())
                .addValue("language", comic.getLanguage())
                .addValue("status", comic.getStatus())
                .addValue("rating", comic.getRating())
                .addValue("readCount", comic.getReadCount())
                .addValue("lastReadAt", comic.getLastReadAt())
                .addValue("lastReadPage", comic.getLastReadPage())
                .addValue("fileSize", comic.getFileSize())
                .addValue("fileModifiedAt", comic.getFileModifiedAt())
                .addValue("createdAt", LocalDateTime.now())
                .addValue("updatedAt", LocalDateTime.now());
        namedJdbc.update(sql, params, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void update(Comic comic) {
        String sql = "UPDATE comic SET source_id = :sourceId, title = :title, author = :author, "
                + "publisher = :publisher, description = :description, cover_path = :coverPath, "
                + "file_path = :filePath, file_type = :fileType, page_count = :pageCount, "
                + "language = :language, status = :status, rating = :rating, read_count = :readCount, "
                + "last_read_at = :lastReadAt, last_read_page = :lastReadPage, "
                + "file_size = :fileSize, file_modified_at = :fileModifiedAt, "
                + "updated_at = :updatedAt WHERE id = :id";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", comic.getId())
                .addValue("sourceId", comic.getSourceId())
                .addValue("title", comic.getTitle())
                .addValue("author", comic.getAuthor())
                .addValue("publisher", comic.getPublisher())
                .addValue("description", comic.getDescription())
                .addValue("coverPath", comic.getCoverPath())
                .addValue("filePath", comic.getFilePath())
                .addValue("fileType", comic.getFileType())
                .addValue("pageCount", comic.getPageCount())
                .addValue("language", comic.getLanguage())
                .addValue("status", comic.getStatus())
                .addValue("rating", comic.getRating())
                .addValue("readCount", comic.getReadCount())
                .addValue("lastReadAt", comic.getLastReadAt())
                .addValue("lastReadPage", comic.getLastReadPage())
                .addValue("fileSize", comic.getFileSize())
                .addValue("fileModifiedAt", comic.getFileModifiedAt())
                .addValue("updatedAt", LocalDateTime.now());
        namedJdbc.update(sql, params);
    }

    public void deleteById(long id) {
        namedJdbc.getJdbcTemplate().update("DELETE FROM comic WHERE id = ?", id);
    }

    public void updateRating(long id, BigDecimal rating) {
        namedJdbc.getJdbcTemplate().update("UPDATE comic SET rating = ?, updated_at = ? WHERE id = ?",
                rating, LocalDateTime.now(), id);
    }

    public void updateReadProgress(long id, int lastReadPage) {
        LocalDateTime now = LocalDateTime.now();
        namedJdbc.getJdbcTemplate().update(
                "UPDATE comic SET last_read_page = ?, last_read_at = ?, updated_at = ? WHERE id = ?",
                lastReadPage, now, now, id);
    }

    public void incrementReadCount(long id) {
        LocalDateTime now = LocalDateTime.now();
        namedJdbc.getJdbcTemplate().update(
                "UPDATE comic SET read_count = read_count + 1, last_read_at = ?, updated_at = ? WHERE id = ?",
                now, now, id);
    }

    public int countBySourceId(long sourceId) {
        return namedJdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM comic WHERE source_id = ?", Integer.class, sourceId);
    }

    public List<Comic> findRecentRead(int limit) {
        return namedJdbc.getJdbcTemplate().query(
                "SELECT * FROM comic WHERE last_read_at IS NOT NULL ORDER BY last_read_at DESC LIMIT ?",
                rowMapper, limit);
    }

    public boolean existsByFilePath(String filePath) {
        Integer count = namedJdbc.getJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM comic WHERE file_path = ?", Integer.class, filePath);
        return count != null && count > 0;
    }

    public boolean existsBySourceAndFilePath(long sourceId, String filePath) {
        Integer count = namedJdbc.getJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM comic WHERE source_id = ? AND file_path = ?",
                Integer.class, sourceId, filePath);
        return count != null && count > 0;
    }

    public Comic findByFilePath(String filePath) {
        try {
            return namedJdbc.getJdbcTemplate().queryForObject(
                    "SELECT * FROM comic WHERE file_path = ?", rowMapper, filePath);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Comic findNextInSource(long comicId, long sourceId) {
        try {
            return namedJdbc.getJdbcTemplate().queryForObject(
                    "SELECT * FROM comic WHERE source_id = ? AND id > ? ORDER BY id ASC LIMIT 1",
                    rowMapper, sourceId, comicId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Comic findPrevInSource(long comicId, long sourceId) {
        try {
            return namedJdbc.getJdbcTemplate().queryForObject(
                    "SELECT * FROM comic WHERE source_id = ? AND id < ? ORDER BY id DESC LIMIT 1",
                    rowMapper, sourceId, comicId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public long count() {
        Long count = namedJdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM comic", Long.class);
        return count != null ? count : 0;
    }
}
