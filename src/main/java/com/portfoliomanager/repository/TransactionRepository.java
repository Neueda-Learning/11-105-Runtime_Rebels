package com.portfoliomanager.repository;

import com.portfoliomanager.mapper.TransactionRowMapper;
import com.portfoliomanager.model.Transaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TransactionRowMapper rowMapper = new TransactionRowMapper();

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Transaction> findByInvestmentId(Long userId, Long investmentId) {
        return jdbcTemplate.query(
                "SELECT t.* FROM transactions t JOIN investments i ON i.id = t.investment_id WHERE i.user_id = ? AND t.investment_id = ? ORDER BY t.transaction_date DESC, t.id DESC",
                rowMapper, userId, investmentId);
    }

    public List<Transaction> findAll(Long userId) {
        return jdbcTemplate.query(
                "SELECT t.* FROM transactions t JOIN investments i ON i.id = t.investment_id WHERE i.user_id = ? ORDER BY t.transaction_date DESC, t.id DESC", rowMapper, userId);
    }

    public Optional<Transaction> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM transactions WHERE id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public Transaction save(Transaction tx) {
        String sql = """
                INSERT INTO transactions
                    (investment_id, type, quantity, price, amount, realized_pl, currency, transaction_date, notes)
                VALUES (?,?,?,?,?,?,?,?,?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, tx.getInvestmentId());
            ps.setString(2, tx.getType().name());
            ps.setBigDecimal(3, tx.getQuantity());
            ps.setBigDecimal(4, tx.getPrice());
            ps.setBigDecimal(5, tx.getAmount());
            ps.setBigDecimal(6, tx.getRealizedPl());
            ps.setString(7, tx.getCurrency());
            ps.setObject(8, tx.getTransactionDate());
            ps.setString(9, tx.getNotes());
            return ps;
        }, keyHolder);

        Long newId = keyHolder.getKey().longValue();
        return findById(newId).orElseThrow();
    }

    /** Sum of realized P/L across all SELL transactions, in instrument currency per row - conversion happens in the service layer. */
    public List<Transaction> findAllRealizedPlTransactions(Long userId) {
        return jdbcTemplate.query(
                "SELECT t.* FROM transactions t JOIN investments i ON i.id = t.investment_id WHERE i.user_id = ? AND t.type = 'SELL' AND t.realized_pl IS NOT NULL", rowMapper, userId);
    }

    public boolean deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM transactions WHERE id = ?", id) > 0;
    }
}