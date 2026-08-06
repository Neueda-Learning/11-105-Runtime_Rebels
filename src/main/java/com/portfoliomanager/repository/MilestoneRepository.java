package com.portfoliomanager.repository;

import com.portfoliomanager.mapper.MilestoneRowMapper;
import com.portfoliomanager.model.Milestone;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class MilestoneRepository {

    private final JdbcTemplate jdbcTemplate;
    private final MilestoneRowMapper rowMapper = new MilestoneRowMapper();

    public MilestoneRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Milestone> findAll(Long userId) {
        return jdbcTemplate.query("SELECT * FROM milestones WHERE user_id = ? ORDER BY threshold_value_base ASC", rowMapper, userId);
    }

    public Optional<Milestone> findById(Long userId, Long id) {
        return jdbcTemplate.query("SELECT * FROM milestones WHERE user_id = ? AND id = ?", rowMapper, userId, id).stream().findFirst();
    }

    public Milestone save(Long userId, Milestone m) {
        String sql = "INSERT INTO milestones (user_id, name, threshold_value_base, comparison_label, achieved, achieved_date) VALUES (?,?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, m.getName());
            ps.setBigDecimal(3, m.getThresholdValueBase());
            ps.setString(4, m.getComparisonLabel());
            ps.setBoolean(5, m.isAchieved());
            ps.setObject(6, m.getAchievedDate());
            return ps;
        }, keyHolder);
        return findById(userId, keyHolder.getKey().longValue()).orElseThrow();
    }

    public void markAchieved(Long userId, Long id, LocalDate achievedDate) {
        jdbcTemplate.update(
                "UPDATE milestones SET achieved = TRUE, achieved_date = ? WHERE user_id = ? AND id = ?", achievedDate, userId, id);
    }

    public boolean deleteById(Long userId, Long id) {
        return jdbcTemplate.update("DELETE FROM milestones WHERE user_id = ? AND id = ?", userId, id) > 0;
    }
}