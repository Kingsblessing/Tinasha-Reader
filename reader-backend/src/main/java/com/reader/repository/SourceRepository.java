package com.reader.repository;

import com.reader.model.entity.Source;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SourceRepository {

    private final NamedParameterJdbcTemplate namedJdbc;
    private final RowMapper<Source> rowMapper = new BeanPropertyRowMapper<>(Source.class);

    public SourceRepository(NamedParameterJdbcTemplate namedJdbc) {
        this.namedJdbc = namedJdbc;
    }

    public List<Source> findAll() {
        return namedJdbc.getJdbcTemplate().query("SELECT * FROM source ORDER BY created_at DESC", rowMapper);
    }

    public Source findById(long id) {
        try {
            return namedJdbc.getJdbcTemplate().queryForObject("SELECT * FROM source WHERE id = ?", rowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public long save(Source source) {
        String sql = "INSERT INTO source (name, path, type, enabled, scan_status, last_scan_at, "
                + "scan_interval_min, comic_count, created_at, updated_at) "
                + "VALUES (:name, :path, :type, :enabled, :scanStatus, :lastScanAt, "
                + ":scanIntervalMin, :comicCount, :createdAt, :updatedAt)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", source.getName())
                .addValue("path", source.getPath())
                .addValue("type", source.getType())
                .addValue("enabled", source.isEnabled())
                .addValue("scanStatus", source.getScanStatus())
                .addValue("lastScanAt", source.getLastScanAt())
                .addValue("scanIntervalMin", source.getScanIntervalMin())
                .addValue("comicCount", source.getComicCount())
                .addValue("createdAt", LocalDateTime.now())
                .addValue("updatedAt", LocalDateTime.now());
        namedJdbc.update(sql, params, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void update(Source source) {
        String sql = "UPDATE source SET name = :name, path = :path, type = :type, enabled = :enabled, "
                + "scan_status = :scanStatus, last_scan_at = :lastScanAt, "
                + "scan_interval_min = :scanIntervalMin, comic_count = :comicCount, "
                + "updated_at = :updatedAt WHERE id = :id";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", source.getId())
                .addValue("name", source.getName())
                .addValue("path", source.getPath())
                .addValue("type", source.getType())
                .addValue("enabled", source.isEnabled())
                .addValue("scanStatus", source.getScanStatus())
                .addValue("lastScanAt", source.getLastScanAt())
                .addValue("scanIntervalMin", source.getScanIntervalMin())
                .addValue("comicCount", source.getComicCount())
                .addValue("updatedAt", LocalDateTime.now());
        namedJdbc.update(sql, params);
    }

    public void deleteById(long id) {
        namedJdbc.getJdbcTemplate().update("DELETE FROM source WHERE id = ?", id);
    }

    public void updateScanStatus(long id, String status) {
        LocalDateTime now = LocalDateTime.now();
        namedJdbc.getJdbcTemplate().update(
                "UPDATE source SET scan_status = ?, last_scan_at = ?, updated_at = ? WHERE id = ?",
                status, now, now, id);
    }

    public void incrementComicCount(long id, int delta) {
        namedJdbc.getJdbcTemplate().update(
                "UPDATE source SET comic_count = comic_count + ?, updated_at = ? WHERE id = ?",
                delta, LocalDateTime.now(), id);
    }

    public long count() {
        Long count = namedJdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM source", Long.class);
        return count != null ? count : 0;
    }
}
