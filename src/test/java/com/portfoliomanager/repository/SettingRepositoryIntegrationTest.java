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

    private static final Long USER_ID = 1L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SettingRepository settingRepository;

    @BeforeEach
    void setUpSchema() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS app_settings");
        jdbcTemplate.execute("""
                CREATE TABLE app_settings (
                    user_id BIGINT NOT NULL,
                    setting_key VARCHAR(50) NOT NULL,
                    setting_value VARCHAR(100) NOT NULL,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (user_id, setting_key)
                )
                """);
    }

    @Test
    void get_returnsEmptyWhenMissing() {
        Optional<String> value = settingRepository.get(USER_ID, "base_currency");

        assertTrue(value.isEmpty());
    }

    @Test
    void set_insertsWhenKeyMissing() {
        settingRepository.set(USER_ID, "base_currency", "INR");

        assertEquals(Optional.of("INR"), settingRepository.get(USER_ID, "base_currency"));
        Integer count = jdbcTemplate
            .queryForObject("SELECT COUNT(*) FROM app_settings WHERE user_id = ? AND setting_key='base_currency'", Integer.class, USER_ID);
        assertEquals(1, count);
    }

    @Test
    void set_updatesWhenKeyExists() {
        jdbcTemplate.update("INSERT INTO app_settings(user_id, setting_key, setting_value, updated_at) VALUES (?,?,?,?)",
            USER_ID, "base_currency", "INR", java.sql.Timestamp.valueOf("2026-08-01 00:00:00"));

        settingRepository.set(USER_ID, "base_currency", "USD");

        assertEquals(Optional.of("USD"), settingRepository.get(USER_ID, "base_currency"));
        Integer count = jdbcTemplate
            .queryForObject("SELECT COUNT(*) FROM app_settings WHERE user_id = ? AND setting_key='base_currency'", Integer.class, USER_ID);
        assertEquals(1, count);
    }
}
