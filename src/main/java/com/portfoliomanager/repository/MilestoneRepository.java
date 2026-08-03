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

    public List<Milestone> findAll() {
        return jdbcTemplate.query("SELECT * FROM milestones ORDER BY threshold_value_base ASC", rowMapper);
    }

    public Optional<Milestone> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM milestones WHERE id = ?", rowMapper, id).stream().findFirst();
    }

    public Milestone save(Milestone m) {
        String sql = "INSERT INTO milestones (name, threshold_value_base, comparison_label, achieved, achieved_date) VALUES (?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, m.getName());
            ps.setBigDecimal(2, m.getThresholdValueBase());
            ps.setString(3, m.getComparisonLabel());
            ps.setBoolean(4, m.isAchieved());
            ps.setObject(5, m.getAchievedDate());
            return ps;
        }, keyHolder);
        return findById(keyHolder.getKey().longValue()).orElseThrow();
    }

    public void markAchieved(Long id, LocalDate achievedDate) {
        jdbcTemplate.update(
                "UPDATE milestones SET achieved = TRUE, achieved_date = ? WHERE id = ?", achievedDate, id);
    }

    public boolean deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM milestones WHERE id = ?", id) > 0;
    }
}