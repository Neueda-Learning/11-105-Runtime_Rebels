package com.portfoliomanager.mapper;

import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.model.TransactionType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionRowMapper implements RowMapper<Transaction> {

    @Override
    public Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Transaction.builder()
                .id(rs.getLong("id"))
                .investmentId(rs.getLong("investment_id"))
                .type(TransactionType.valueOf(rs.getString("type")))
                .quantity(rs.getBigDecimal("quantity"))
                .price(rs.getBigDecimal("price"))
                .amount(rs.getBigDecimal("amount"))
                .realizedPl(rs.getBigDecimal("realized_pl"))
                .currency(rs.getString("currency"))
                .transactionDate(rs.getDate("transaction_date").toLocalDate())
                .notes(rs.getString("notes"))
                .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
                .build();
    }
}
