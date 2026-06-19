package com.reader.repository;

import com.reader.model.entity.Setting;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SettingRepository {

    private final NamedParameterJdbcTemplate namedJdbc;
    private final RowMapper<Setting> rowMapper = new BeanPropertyRowMapper<>(Setting.class);

    public SettingRepository(NamedParameterJdbcTemplate namedJdbc) {
        this.namedJdbc = namedJdbc;
    }

    public List<Setting> findAll() {
        return namedJdbc.getJdbcTemplate().query("SELECT * FROM `setting`", rowMapper);
    }

    public Setting findByKey(String key) {
        try {
            return namedJdbc.getJdbcTemplate().queryForObject(
                    "SELECT * FROM `setting` WHERE `key` = ?", rowMapper, key);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void save(Setting setting) {
        String sql = "INSERT INTO `setting` (`key`, `value`, updated_at) VALUES (:key, :value, :updatedAt) "
                + "ON DUPLICATE KEY UPDATE `value` = :value, updated_at = :updatedAt";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("key", setting.getKey())
                .addValue("value", setting.getValue())
                .addValue("updatedAt", LocalDateTime.now());
        namedJdbc.update(sql, params);
    }

    public void deleteByKey(String key) {
        namedJdbc.getJdbcTemplate().update("DELETE FROM `setting` WHERE `key` = ?", key);
    }

    public long count() {
        Long count = namedJdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM `setting`", Long.class);
        return count != null ? count : 0;
    }
}
