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

    public List<PortfolioSnapshot> findBetween(LocalDate from, LocalDate to) {
        return jdbcTemplate.query(
                "SELECT * FROM portfolio_snapshots WHERE snapshot_date BETWEEN ? AND ? ORDER BY snapshot_date ASC",
                rowMapper, from, to);
    }

    public List<PortfolioSnapshot> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM portfolio_snapshots ORDER BY snapshot_date ASC", rowMapper);
    }

    /** Insert today's snapshot, or update it if one already exists for the date (idempotent). */
    public void upsert(PortfolioSnapshot s) {
        int updated = jdbcTemplate.update(
                """
                UPDATE portfolio_snapshots SET total_invested_base = ?, total_value_base = ?,
                    realized_pl_base = ?, unrealized_pl_base = ? WHERE snapshot_date = ?
                """,
                s.getTotalInvestedBase(), s.getTotalValueBase(), s.getRealizedPlBase(),
                s.getUnrealizedPlBase(), s.getSnapshotDate());

        if (updated == 0) {
            jdbcTemplate.update(
                    """
                    INSERT INTO portfolio_snapshots
                        (snapshot_date, total_invested_base, total_value_base, realized_pl_base, unrealized_pl_base)
                    VALUES (?,?,?,?,?)
                    """,
                    s.getSnapshotDate(), s.getTotalInvestedBase(), s.getTotalValueBase(),
                    s.getRealizedPlBase(), s.getUnrealizedPlBase());
        }
    }
}