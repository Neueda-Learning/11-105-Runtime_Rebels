package com.portfoliomanager.mapper;

import com.portfoliomanager.model.ExchangeRate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExchangeRateRowMapper implements RowMapper<ExchangeRate> {

    @Override
    public ExchangeRate mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ExchangeRate.builder()
                .id(rs.getLong("id"))
                .currencyCode(rs.getString("currency_code"))
                .rateToBase(rs.getBigDecimal("rate_to_base"))
                .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
                .build();
    }
}
