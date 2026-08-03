package com.portfoliomanager.mapper;

import com.portfoliomanager.model.PortfolioSnapshot;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PortfolioSnapshotRowMapper implements RowMapper<PortfolioSnapshot> {

    @Override
    public PortfolioSnapshot mapRow(ResultSet rs, int rowNum) throws SQLException {
        return PortfolioSnapshot.builder()
                .id(rs.getLong("id"))
                .snapshotDate(rs.getDate("snapshot_date").toLocalDate())
                .totalInvestedBase(rs.getBigDecimal("total_invested_base"))
                .totalValueBase(rs.getBigDecimal("total_value_base"))
                .realizedPlBase(rs.getBigDecimal("realized_pl_base"))
                .unrealizedPlBase(rs.getBigDecimal("unrealized_pl_base"))
                .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
                .build();
    }
}
