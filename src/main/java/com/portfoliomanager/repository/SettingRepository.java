package com.portfoliomanager.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SettingRepository {

    private final JdbcTemplate jdbcTemplate;

    public SettingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<String> get(Long userId, String key) {
        return jdbcTemplate.query(
                "SELECT setting_value FROM app_settings WHERE user_id = ? AND setting_key = ?",
                rs -> rs.next() ? Optional.of(rs.getString(1)) : Optional.empty(), userId, key);
    }

    public void set(Long userId, String key, String value) {
        int updated = jdbcTemplate.update(
                "UPDATE app_settings SET setting_value = ? WHERE user_id = ? AND setting_key = ?", value, userId, key);
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO app_settings (user_id, setting_key, setting_value) VALUES (?,?,?)", userId, key,
                    value);
        }
    }
}