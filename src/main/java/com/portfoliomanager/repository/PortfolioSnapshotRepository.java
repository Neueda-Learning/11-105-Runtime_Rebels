package com.portfoliomanager.repository;

import com.portfoliomanager.mapper.PortfolioSnapshotRowMapper;
import com.portfoliomanager.model.PortfolioSnapshot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class PortfolioSnapshotRepository {

    private final JdbcTemplate jdbcTemplate;
    private final PortfolioSnapshotRowMapper rowMapper = new PortfolioSnapshotRowMapper();

    public PortfolioSnapshotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PortfolioSnapshot> findBetween(Long userId, LocalDate from, LocalDate to) {
        return jdbcTemplate.query(
                "SELECT * FROM portfolio_snapshots WHERE user_id = ? AND snapshot_date BETWEEN ? AND ? ORDER BY snapshot_date ASC",
                rowMapper, userId, from, to);
    }

    public List<PortfolioSnapshot> findAll(Long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM portfolio_snapshots WHERE user_id = ? ORDER BY snapshot_date ASC", rowMapper, userId);
    }

    /** Insert today's snapshot, or update it if one already exists for the date (idempotent). */
    public void upsert(Long userId, PortfolioSnapshot s) {
        int updated = jdbcTemplate.update(
                """
                UPDATE portfolio_snapshots SET total_invested_base = ?, total_value_base = ?,
                    realized_pl_base = ?, unrealized_pl_base = ? WHERE user_id = ? AND snapshot_date = ?
                """,
                s.getTotalInvestedBase(), s.getTotalValueBase(), s.getRealizedPlBase(),
                s.getUnrealizedPlBase(), userId, s.getSnapshotDate());

        if (updated == 0) {
            jdbcTemplate.update(
                    """
                    INSERT INTO portfolio_snapshots
                        (user_id, snapshot_date, total_invested_base, total_value_base, realized_pl_base, unrealized_pl_base)
                    VALUES (?,?,?,?,?,?)
                    """,
                    userId, s.getSnapshotDate(), s.getTotalInvestedBase(), s.getTotalValueBase(),
                    s.getRealizedPlBase(), s.getUnrealizedPlBase());
        }
    }
}