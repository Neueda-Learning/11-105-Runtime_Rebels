package com.portfoliomanager.mapper;

import com.portfoliomanager.model.Milestone;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MilestoneRowMapper implements RowMapper<Milestone> {

    @Override
    public Milestone mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Milestone.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .thresholdValueBase(rs.getBigDecimal("threshold_value_base"))
                .comparisonLabel(rs.getString("comparison_label"))
                .achieved(rs.getBoolean("achieved"))
                .achievedDate(rs.getDate("achieved_date") != null ? rs.getDate("achieved_date").toLocalDate() : null)
                .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
                .build();
    }
}
