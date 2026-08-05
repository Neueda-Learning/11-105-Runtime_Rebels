package com.portfoliomanager.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest(properties = "spring.flyway.enabled=false")
@Import(SettingRepository.class)
class SettingRepositoryIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SettingRepository settingRepository;

    @BeforeEach
    void setUpSchema() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS app_settings");
        jdbcTemplate.execute("""
                CREATE TABLE app_settings (
                    setting_key VARCHAR(50) NOT NULL PRIMARY KEY,
                    setting_value VARCHAR(100) NOT NULL,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    @Test
    void get_returnsEmptyWhenMissing() {
        Optional<String> value = settingRepository.get("base_currency");

        assertTrue(value.isEmpty());
    }

    @Test
    void set_insertsWhenKeyMissing() {
        settingRepository.set("base_currency", "INR");

        assertEquals(Optional.of("INR"), settingRepository.get("base_currency"));
        Integer count = jdbcTemplate
                .queryForObject("SELECT COUNT(*) FROM app_settings WHERE setting_key='base_currency'", Integer.class);
        assertEquals(1, count);
    }

    @Test
    void set_updatesWhenKeyExists() {
        jdbcTemplate.update("INSERT INTO app_settings(setting_key, setting_value, updated_at) VALUES (?,?,?)",
                "base_currency", "INR", java.sql.Timestamp.valueOf("2026-08-01 00:00:00"));

        settingRepository.set("base_currency", "USD");

        assertEquals(Optional.of("USD"), settingRepository.get("base_currency"));
        Integer count = jdbcTemplate
                .queryForObject("SELECT COUNT(*) FROM app_settings WHERE setting_key='base_currency'", Integer.class);
        assertEquals(1, count);
    }
}
