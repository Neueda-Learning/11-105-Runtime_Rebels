package com.portfoliomanager.repository;

import com.portfoliomanager.mapper.InvestmentRowMapper;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentStatus;
import com.portfoliomanager.model.InvestmentType;
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

    public List<Investment> findAll(Long userId, InvestmentType type, String country, InvestmentStatus status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM investments WHERE user_id = ?");
        List<Object> params = new java.util.ArrayList<>();
        params.add(userId);

        if (type != null) {
            sql.append(" AND type = ?");
            params.add(type.name());
        }
        if (country != null && !country.isBlank()) {
            sql.append(" AND country = ?");
            params.add(country);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.name());
        }
        sql.append(" ORDER BY created_at DESC");

        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public List<Investment> findAllActive(Long userId) {
        return jdbcTemplate.query("SELECT * FROM investments WHERE user_id = ? AND status = 'ACTIVE'", rowMapper, userId);
    }

    public Optional<Investment> findById(Long userId, Long id) {
        List<Investment> results = jdbcTemplate.query(
                "SELECT * FROM investments WHERE user_id = ? AND id = ?", rowMapper, userId, id);
        return results.stream().findFirst();
    }

    public Investment save(Long userId, Investment inv) {
        String sql = """
                INSERT INTO investments
                    (user_id, type, symbol, name, country, currency, quantity, avg_buy_price, current_price,
                     invested_amount, current_value, previous_value, interest_rate, maturity_date,
                     purchase_date, status, notes)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, inv.getType().name());
            ps.setString(3, inv.getSymbol());
            ps.setString(4, inv.getName());
            ps.setString(5, inv.getCountry());
            ps.setString(6, inv.getCurrency());
            ps.setBigDecimal(7, inv.getQuantity());
            ps.setBigDecimal(8, inv.getAvgBuyPrice());
            ps.setBigDecimal(9, inv.getCurrentPrice());
            ps.setBigDecimal(10, inv.getInvestedAmount());
            ps.setBigDecimal(11, inv.getCurrentValue());
            ps.setBigDecimal(12, inv.getPreviousValue());
            ps.setBigDecimal(13, inv.getInterestRate());
            ps.setObject(14, inv.getMaturityDate());
            ps.setObject(15, inv.getPurchaseDate());
            ps.setString(16, inv.getStatus().name());
            ps.setString(17, inv.getNotes());
            return ps;
        }, keyHolder);

        Long newId = keyHolder.getKey().longValue();
        return findById(userId, newId).orElseThrow();
    }

    public Investment update(Long userId, Investment inv) {
        String sql = """
                UPDATE investments SET
                    type = ?, symbol = ?, name = ?, country = ?, currency = ?,
                    quantity = ?, avg_buy_price = ?, current_price = ?,
                    invested_amount = ?, current_value = ?, previous_value = ?,
                    interest_rate = ?, maturity_date = ?, purchase_date = ?,
                    status = ?, notes = ?
                WHERE user_id = ? AND id = ?
                """;
        jdbcTemplate.update(sql,
                inv.getType().name(), inv.getSymbol(), inv.getName(), inv.getCountry(), inv.getCurrency(),
                inv.getQuantity(), inv.getAvgBuyPrice(), inv.getCurrentPrice(),
                inv.getInvestedAmount(), inv.getCurrentValue(), inv.getPreviousValue(),
                inv.getInterestRate(), inv.getMaturityDate(), inv.getPurchaseDate(),
                inv.getStatus().name(), inv.getNotes(), userId, inv.getId());
            return findById(userId, inv.getId()).orElseThrow();
    }

    /** Roll every active investment's current_value into previous_value - used by the daily snapshot job. */
    public void rollCurrentValueIntoPrevious(Long userId) {
        jdbcTemplate.update("UPDATE investments SET previous_value = current_value WHERE user_id = ? AND status = 'ACTIVE'", userId);
    }

    public void updatePrice(Long userId, Long id, java.math.BigDecimal currentPrice, java.math.BigDecimal currentValue) {
        jdbcTemplate.update(
                "UPDATE investments SET current_price = ?, current_value = ? WHERE user_id = ? AND id = ?",
                currentPrice, currentValue, userId, id);
    }

    public boolean deleteById(Long userId, Long id) {
        int rows = jdbcTemplate.update("DELETE FROM investments WHERE user_id = ? AND id = ?", userId, id);
        return rows > 0;
    }

    public boolean existsById(Long userId, Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM investments WHERE user_id = ? AND id = ?", Integer.class, userId, id);
        return count != null && count > 0;
    }
}