package com.reader.repository;

import com.reader.model.entity.Review;
import org.springframework.dao.EmptyResultDataAccessException;
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

@Repository
public class ReviewRepository {

    private final NamedParameterJdbcTemplate namedJdbc;
    private final RowMapper<Review> rowMapper = new BeanPropertyRowMapper<>(Review.class);

    public ReviewRepository(NamedParameterJdbcTemplate namedJdbc) {
        this.namedJdbc = namedJdbc;
    }

    public List<Review> findByComicId(long comicId) {
        return namedJdbc.getJdbcTemplate().query(
                "SELECT * FROM review WHERE comic_id = ? ORDER BY created_at DESC", rowMapper, comicId);
    }

    public Review findById(long id) {
        try {
            return namedJdbc.getJdbcTemplate().queryForObject("SELECT * FROM review WHERE id = ?", rowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public long save(Review review) {
        String sql = "INSERT INTO review (comic_id, page_number, content, created_at, updated_at) "
                + "VALUES (:comicId, :pageNumber, :content, :createdAt, :updatedAt)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("comicId", review.getComicId())
                .addValue("pageNumber", review.getPageNumber())
                .addValue("content", review.getContent())
                .addValue("createdAt", now)
                .addValue("updatedAt", now);
        namedJdbc.update(sql, params, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void update(Review review) {
        String sql = "UPDATE review SET content = :content, updated_at = :updatedAt WHERE id = :id";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", review.getId())
                .addValue("content", review.getContent())
                .addValue("updatedAt", LocalDateTime.now());
        namedJdbc.update(sql, params);
    }

    public void deleteById(long id) {
        namedJdbc.getJdbcTemplate().update("DELETE FROM review WHERE id = ?", id);
    }

    public long count() {
        Long count = namedJdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM review", Long.class);
        return count != null ? count : 0;
    }
}
