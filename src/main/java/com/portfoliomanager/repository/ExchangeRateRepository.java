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

    public List<ExchangeRate> findAll() {
        return jdbcTemplate.query("SELECT * FROM exchange_rates ORDER BY currency_code", rowMapper);
    }

    public Optional<ExchangeRate> findByCurrencyCode(String currencyCode) {
        return jdbcTemplate.query("SELECT * FROM exchange_rates WHERE currency_code = ?", rowMapper, currencyCode)
                .stream().findFirst();
    }

    public ExchangeRate upsert(String currencyCode, BigDecimal rateToBase) {
        int updated = jdbcTemplate.update(
                "UPDATE exchange_rates SET rate_to_base = ? WHERE currency_code = ?", rateToBase, currencyCode);
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO exchange_rates (currency_code, rate_to_base) VALUES (?,?)",
                    currencyCode, rateToBase);
        }
        return findByCurrencyCode(currencyCode).orElseThrow();
    }
}