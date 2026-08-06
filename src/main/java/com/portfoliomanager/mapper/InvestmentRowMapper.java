package com.portfoliomanager.mapper;

import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentStatus;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.model.CommodityType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InvestmentRowMapper implements RowMapper<Investment> {

    @Override
    public Investment mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Investment.builder()
                .id(rs.getLong("id"))
                .type(InvestmentType.valueOf(rs.getString("type")))
                .symbol(rs.getString("symbol"))
                .name(rs.getString("name"))
                .country(rs.getString("country"))
                .currency(rs.getString("currency"))
                .market(rs.getString("market_exchange"))
                .commodityType(rs.getString("commodity_type") != null ? CommodityType.valueOf(rs.getString("commodity_type")) : null)
                .quantity(rs.getBigDecimal("quantity"))
                .avgBuyPrice(rs.getBigDecimal("avg_buy_price"))
                .currentPrice(rs.getBigDecimal("current_price"))
                .investedAmount(rs.getBigDecimal("invested_amount"))
                .currentValue(rs.getBigDecimal("current_value"))
                .previousValue(rs.getBigDecimal("previous_value"))
                .interestRate(rs.getBigDecimal("interest_rate"))
                .maturityDate(rs.getDate("maturity_date") != null ? rs.getDate("maturity_date").toLocalDate() : null)
                .purchaseDate(rs.getDate("purchase_date") != null ? rs.getDate("purchase_date").toLocalDate() : null)
                .status(InvestmentStatus.valueOf(rs.getString("status")))
                .notes(rs.getString("notes"))
                .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
                .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
                .build();
    }
}
