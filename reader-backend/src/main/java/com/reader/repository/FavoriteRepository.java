package com.reader.repository;

import com.reader.model.entity.Favorite;
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
public class FavoriteRepository {

    private final NamedParameterJdbcTemplate namedJdbc;
    private final RowMapper<Favorite> rowMapper = new BeanPropertyRowMapper<>(Favorite.class);

    public FavoriteRepository(NamedParameterJdbcTemplate namedJdbc) {
        this.namedJdbc = namedJdbc;
    }

    public List<Favorite> findAll() {
        return namedJdbc.getJdbcTemplate().query("SELECT * FROM favorite ORDER BY created_at DESC", rowMapper);
    }

    public List<Favorite> findByGroupName(String groupName) {
        return namedJdbc.getJdbcTemplate().query(
                "SELECT * FROM favorite WHERE group_name = ? ORDER BY created_at DESC", rowMapper, groupName);
    }

    public List<Favorite> findByComicId(long comicId) {
        return namedJdbc.getJdbcTemplate().query(
                "SELECT * FROM favorite WHERE comic_id = ? ORDER BY created_at DESC", rowMapper, comicId);
    }

    public Favorite findById(long id) {
        try {
            return namedJdbc.getJdbcTemplate().queryForObject("SELECT * FROM favorite WHERE id = ?", rowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public long save(Favorite favorite) {
        String sql = "INSERT INTO favorite (comic_id, group_name, created_at) "
                + "VALUES (:comicId, :groupName, :createdAt)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("comicId", favorite.getComicId())
                .addValue("groupName", favorite.getGroupName())
                .addValue("createdAt", LocalDateTime.now());
        namedJdbc.update(sql, params, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void deleteById(long id) {
        namedJdbc.getJdbcTemplate().update("DELETE FROM favorite WHERE id = ?", id);
    }

    public boolean existsByComicIdAndGroupName(long comicId, String groupName) {
        Integer count = namedJdbc.getJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM favorite WHERE comic_id = ? AND group_name = ?",
                Integer.class, comicId, groupName);
        return count != null && count > 0;
    }

    public List<String> findGroupNames() {
        return namedJdbc.getJdbcTemplate().queryForList(
                "SELECT DISTINCT group_name FROM favorite WHERE group_name IS NOT NULL ORDER BY group_name ASC",
                String.class);
    }

    public long count() {
        Long count = namedJdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM favorite", Long.class);
        return count != null ? count : 0;
    }
}
