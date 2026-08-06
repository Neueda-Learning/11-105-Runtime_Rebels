package com.portfoliomanager.repository;

import com.portfoliomanager.mapper.InvestmentRowMapper;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentStatus;
import com.portfoliomanager.model.InvestmentType;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class InvestmentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final InvestmentRowMapper rowMapper = new InvestmentRowMapper();

    public InvestmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Investment> findAll(InvestmentType type, String country, InvestmentStatus status) {
        StringBuilder sql = new StringBuilder("""
                SELECT i.*, c.commodity_type, c.market_exchange
                FROM investments i
                LEFT JOIN commodities c ON c.investment_id = i.id
                WHERE 1=1
                """);
        List<Object> params = new java.util.ArrayList<>();

        if (type != null) {
            sql.append(" AND i.type = ?");
            params.add(type.name());
        }
        if (country != null && !country.isBlank()) {
            sql.append(" AND i.country = ?");
            params.add(country);
        }
        if (status != null) {
            sql.append(" AND i.status = ?");
            params.add(status.name());
        }
        sql.append(" ORDER BY i.created_at DESC");

        try {
            return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
        } catch (DataAccessException ex) {
            StringBuilder fallback = new StringBuilder("""
                    SELECT i.*, NULL AS commodity_type, NULL AS market_exchange
                    FROM investments i
                    WHERE 1=1
                    """);
            if (type != null) fallback.append(" AND i.type = ?");
            if (country != null && !country.isBlank()) fallback.append(" AND i.country = ?");
            if (status != null) fallback.append(" AND i.status = ?");
            fallback.append(" ORDER BY i.created_at DESC");
            return jdbcTemplate.query(fallback.toString(), rowMapper, params.toArray());
        }
    }

    public List<Investment> findAllActive() {
        try {
            return jdbcTemplate.query(
                    """
                            SELECT i.*, c.commodity_type, c.market_exchange
                            FROM investments i
                            LEFT JOIN commodities c ON c.investment_id = i.id
                            WHERE i.status = 'ACTIVE'
                            """,
                    rowMapper);
        } catch (DataAccessException ex) {
            return jdbcTemplate.query(
                    """
                            SELECT i.*, NULL AS commodity_type, NULL AS market_exchange
                            FROM investments i
                            WHERE i.status = 'ACTIVE'
                            """,
                    rowMapper);
        }
    }

    public Optional<Investment> findById(Long id) {
        List<Investment> results;
        try {
            results = jdbcTemplate.query(
                    """
                            SELECT i.*, c.commodity_type, c.market_exchange
                            FROM investments i
                            LEFT JOIN commodities c ON c.investment_id = i.id
                            WHERE i.id = ?
                            """, rowMapper, id);
        } catch (DataAccessException ex) {
            results = jdbcTemplate.query(
                    """
                            SELECT i.*, NULL AS commodity_type, NULL AS market_exchange
                            FROM investments i
                            WHERE i.id = ?
                            """, rowMapper, id);
        }
        return results.stream().findFirst();
    }

    public Investment save(Investment inv) {
        String sql = """
                INSERT INTO investments
                    (type, symbol, name, country, currency, quantity, avg_buy_price, current_price,
                     invested_amount, current_value, previous_value, interest_rate, maturity_date,
                     purchase_date, status, notes)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, inv.getType().name());
            ps.setString(2, inv.getSymbol());
            ps.setString(3, inv.getName());
            ps.setString(4, inv.getCountry());
            ps.setString(5, inv.getCurrency());
            ps.setBigDecimal(6, inv.getQuantity());
            ps.setBigDecimal(7, inv.getAvgBuyPrice());
            ps.setBigDecimal(8, inv.getCurrentPrice());
            ps.setBigDecimal(9, inv.getInvestedAmount());
            ps.setBigDecimal(10, inv.getCurrentValue());
            ps.setBigDecimal(11, inv.getPreviousValue());
            ps.setBigDecimal(12, inv.getInterestRate());
            ps.setObject(13, inv.getMaturityDate());
            ps.setObject(14, inv.getPurchaseDate());
            ps.setString(15, inv.getStatus().name());
            ps.setString(16, inv.getNotes());
            return ps;
        }, keyHolder);

        Long newId = keyHolder.getKey().longValue();
        return findById(newId).orElseThrow();
    }

    public Investment update(Investment inv) {
        String sql = """
                UPDATE investments SET
                    type = ?, symbol = ?, name = ?, country = ?, currency = ?,
                    quantity = ?, avg_buy_price = ?, current_price = ?,
                    invested_amount = ?, current_value = ?, previous_value = ?,
                    interest_rate = ?, maturity_date = ?, purchase_date = ?,
                    status = ?, notes = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                inv.getType().name(), inv.getSymbol(), inv.getName(), inv.getCountry(), inv.getCurrency(),
                inv.getQuantity(), inv.getAvgBuyPrice(), inv.getCurrentPrice(),
                inv.getInvestedAmount(), inv.getCurrentValue(), inv.getPreviousValue(),
                inv.getInterestRate(), inv.getMaturityDate(), inv.getPurchaseDate(),
                inv.getStatus().name(), inv.getNotes(), inv.getId());
        return findById(inv.getId()).orElseThrow();
    }

    /** Roll every active investment's current_value into previous_value - used by the daily snapshot job. */
    public void rollCurrentValueIntoPrevious() {
        jdbcTemplate.update("UPDATE investments SET previous_value = current_value WHERE status = 'ACTIVE'");
    }

    public void updatePrice(Long id, java.math.BigDecimal currentPrice, java.math.BigDecimal currentValue) {
        jdbcTemplate.update(
                "UPDATE investments SET current_price = ?, current_value = ? WHERE id = ?",
                currentPrice, currentValue, id);
    }

    public boolean deleteById(Long id) {
        int rows = jdbcTemplate.update("DELETE FROM investments WHERE id = ?", id);
        return rows > 0;
    }

    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM investments WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }
}