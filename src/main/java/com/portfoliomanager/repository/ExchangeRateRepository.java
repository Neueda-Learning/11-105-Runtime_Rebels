package com.portfoliomanager.repository;

import com.portfoliomanager.mapper.ExchangeRateRowMapper;
import com.portfoliomanager.model.ExchangeRate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class ExchangeRateRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ExchangeRateRowMapper rowMapper = new ExchangeRateRowMapper();

    public ExchangeRateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ExchangeRate> findAll(Long userId) {
        return jdbcTemplate.query("SELECT * FROM exchange_rates WHERE user_id = ? ORDER BY currency_code", rowMapper, userId);
    }

    public Optional<ExchangeRate> findByCurrencyCode(Long userId, String currencyCode) {
        return jdbcTemplate.query("SELECT * FROM exchange_rates WHERE user_id = ? AND currency_code = ?", rowMapper, userId, currencyCode)
                .stream().findFirst();
    }

    public ExchangeRate upsert(Long userId, String currencyCode, BigDecimal rateToBase) {
        int updated = jdbcTemplate.update(
                "UPDATE exchange_rates SET rate_to_base = ? WHERE user_id = ? AND currency_code = ?", rateToBase, userId, currencyCode);
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO exchange_rates (user_id, currency_code, rate_to_base) VALUES (?,?,?)",
                    userId, currencyCode, rateToBase);
        }
        return findByCurrencyCode(userId, currencyCode).orElseThrow();
    }
}