package com.portfoliomanager.repository;

import com.portfoliomanager.mapper.AppUserRowMapper;
import com.portfoliomanager.model.AppUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AppUserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AppUserRowMapper rowMapper = new AppUserRowMapper();

    public AppUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AppUser> findByGoogleSubject(String googleSubject) {
        return jdbcTemplate.query(
                "SELECT * FROM app_users WHERE google_subject = ?",
                rowMapper,
                googleSubject).stream().findFirst();
    }

    public AppUser upsertByGoogleSubject(String googleSubject, String email, String displayName, String avatarUrl) {
        int updated = jdbcTemplate.update(
                """
                        UPDATE app_users
                        SET email = ?, display_name = ?, avatar_url = ?
                        WHERE google_subject = ?
                        """,
                email, displayName, avatarUrl, googleSubject);

        if (updated == 0) {
            jdbcTemplate.update(
                    """
                            INSERT INTO app_users (google_subject, email, display_name, avatar_url)
                            VALUES (?,?,?,?)
                            """,
                    googleSubject, email, displayName, avatarUrl);
        }

        return findByGoogleSubject(googleSubject).orElseThrow();
    }
}
