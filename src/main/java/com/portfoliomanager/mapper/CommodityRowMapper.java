package com.portfoliomanager.mapper;

import com.portfoliomanager.model.Commodity;
import com.portfoliomanager.model.CommodityType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CommodityRowMapper implements RowMapper<Commodity> {
    @Override
    public Commodity mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Commodity.builder()
                .id(rs.getLong("id"))
                .investmentId(rs.getLong("investment_id"))
                .commodityName(rs.getString("commodity_name"))
                .commodityType(CommodityType.valueOf(rs.getString("commodity_type")))
                .marketExchange(rs.getString("market_exchange"))
                .country(rs.getString("country"))
                .currency(rs.getString("currency"))
                .quantity(rs.getBigDecimal("quantity"))
                .purchasePrice(rs.getBigDecimal("purchase_price"))
                .currentPrice(rs.getBigDecimal("current_price"))
                .purchaseDate(rs.getDate("purchase_date") != null ? rs.getDate("purchase_date").toLocalDate() : null)
                .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
                .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
                .build();
    }
}

