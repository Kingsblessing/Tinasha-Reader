package com.reader.repository;

import com.reader.model.entity.Tag;
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
public class TagRepository {

    private final NamedParameterJdbcTemplate namedJdbc;
    private final RowMapper<Tag> rowMapper = new BeanPropertyRowMapper<>(Tag.class);

    public TagRepository(NamedParameterJdbcTemplate namedJdbc) {
        this.namedJdbc = namedJdbc;
    }

    public List<Tag> findAll() {
        return namedJdbc.getJdbcTemplate().query("SELECT * FROM tag ORDER BY name ASC", rowMapper);
    }

    public Tag findById(long id) {
        try {
            return namedJdbc.getJdbcTemplate().queryForObject("SELECT * FROM tag WHERE id = ?", rowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Tag findByName(String name) {
        try {
            return namedJdbc.getJdbcTemplate().queryForObject("SELECT * FROM tag WHERE name = ?", rowMapper, name);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Tag> findByComicId(long comicId) {
        return namedJdbc.getJdbcTemplate().query(
                "SELECT t.* FROM tag t INNER JOIN comic_tag ct ON t.id = ct.tag_id WHERE ct.comic_id = ? ORDER BY t.name ASC",
                rowMapper, comicId);
    }

    public List<Tag> findByGroupName(String groupName) {
        return namedJdbc.getJdbcTemplate().query(
                "SELECT * FROM tag WHERE group_name = ? ORDER BY name ASC", rowMapper, groupName);
    }

    public long save(Tag tag) {
        String sql = "INSERT INTO tag (name, group_name, color, parent_id, comic_count, created_at) "
                + "VALUES (:name, :groupName, :color, :parentId, :comicCount, :createdAt)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", tag.getName())
                .addValue("groupName", tag.getGroupName())
                .addValue("color", tag.getColor())
                .addValue("parentId", tag.getParentId())
                .addValue("comicCount", tag.getComicCount())
                .addValue("createdAt", LocalDateTime.now());
        namedJdbc.update(sql, params, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void update(Tag tag) {
        String sql = "UPDATE tag SET name = :name, group_name = :groupName, color = :color, "
                + "parent_id = :parentId, comic_count = :comicCount WHERE id = :id";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", tag.getId())
                .addValue("name", tag.getName())
                .addValue("groupName", tag.getGroupName())
                .addValue("color", tag.getColor())
                .addValue("parentId", tag.getParentId())
                .addValue("comicCount", tag.getComicCount());
        namedJdbc.update(sql, params);
    }

    public void deleteById(long id) {
        namedJdbc.getJdbcTemplate().update("DELETE FROM tag WHERE id = ?", id);
    }

    public List<String> findGroupNames() {
        return namedJdbc.getJdbcTemplate().queryForList(
                "SELECT DISTINCT group_name FROM tag WHERE group_name IS NOT NULL ORDER BY group_name ASC",
                String.class);
    }

    public void addComicTag(long comicId, long tagId) {
        namedJdbc.getJdbcTemplate().update("INSERT INTO comic_tag (comic_id, tag_id) VALUES (?, ?)", comicId, tagId);
    }

    public void removeComicTag(long comicId, long tagId) {
        namedJdbc.getJdbcTemplate().update("DELETE FROM comic_tag WHERE comic_id = ? AND tag_id = ?", comicId, tagId);
    }

    public void incrementComicCount(long tagId, int delta) {
        namedJdbc.getJdbcTemplate().update("UPDATE tag SET comic_count = comic_count + ? WHERE id = ?", delta, tagId);
    }

    public long count() {
        Long count = namedJdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM tag", Long.class);
        return count != null ? count : 0;
    }
}
