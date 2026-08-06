package com.portfoliomanager.mapper;

import com.portfoliomanager.model.AppUser;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AppUserRowMapper implements RowMapper<AppUser> {

    @Override
    public AppUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        return AppUser.builder()
                .id(rs.getLong("id"))
                .googleSubject(rs.getString("google_subject"))
                .email(rs.getString("email"))
                .displayName(rs.getString("display_name"))
                .avatarUrl(rs.getString("avatar_url"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}
