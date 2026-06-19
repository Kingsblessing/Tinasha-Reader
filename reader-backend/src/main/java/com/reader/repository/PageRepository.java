package com.reader.repository;

import com.reader.model.entity.Page;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PageRepository {

    private final NamedParameterJdbcTemplate namedJdbc;
    private final RowMapper<Page> rowMapper = new BeanPropertyRowMapper<>(Page.class);

    public PageRepository(NamedParameterJdbcTemplate namedJdbc) {
        this.namedJdbc = namedJdbc;
    }

    public List<Page> findByComicId(long comicId) {
        return namedJdbc.getJdbcTemplate().query(
                "SELECT * FROM page WHERE comic_id = ? ORDER BY page_number ASC", rowMapper, comicId);
    }

    public Page findByComicIdAndPageNumber(long comicId, int pageNumber) {
        try {
            return namedJdbc.getJdbcTemplate().queryForObject(
                    "SELECT * FROM page WHERE comic_id = ? AND page_number = ?",
                    rowMapper, comicId, pageNumber);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void saveAll(List<Page> pages) {
        String sql = "INSERT INTO page (comic_id, page_number, file_name, file_path, "
                + "width, height, file_size) "
                + "VALUES (:comicId, :pageNumber, :fileName, :filePath, "
                + ":width, :height, :fileSize)";
        namedJdbc.batchUpdate(sql, SqlParameterSourceUtils.createBatch(pages.toArray()));
    }

    public void deleteByComicId(long comicId) {
        namedJdbc.getJdbcTemplate().update("DELETE FROM page WHERE comic_id = ?", comicId);
    }

    public int countByComicId(long comicId) {
        return namedJdbc.getJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM page WHERE comic_id = ?", Integer.class, comicId);
    }

    public long count() {
        Long count = namedJdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM page", Long.class);
        return count != null ? count : 0;
    }
}
