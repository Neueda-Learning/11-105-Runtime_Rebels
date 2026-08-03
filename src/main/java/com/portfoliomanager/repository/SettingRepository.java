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

    public Optional<String> get(String key) {
        return jdbcTemplate.query(
                "SELECT setting_value FROM app_settings WHERE setting_key = ?",
                rs -> rs.next() ? Optional.of(rs.getString(1)) : Optional.empty(), key);
    }

    public void set(String key, String value) {
        int updated = jdbcTemplate.update(
                "UPDATE app_settings SET setting_value = ? WHERE setting_key = ?", value, key);
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO app_settings (setting_key, setting_value) VALUES (?,?)", key, value);
        }
    }
}