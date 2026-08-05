package com.portfoliomanager.repository;

import com.portfoliomanager.mapper.TransactionRowMapper;
import com.portfoliomanager.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TransactionRepository transactionRepository;

    @Test
    void findAllRealizedPlTransactions_usesSellAndRealizedPlFilterQuery() {
        List<Transaction> expected = List.of(Transaction.builder().id(11L).build());

        when(jdbcTemplate.query(
                eq("SELECT * FROM transactions WHERE type = 'SELL' AND realized_pl IS NOT NULL"),
                any(TransactionRowMapper.class)))
                .thenReturn(expected);

        List<Transaction> result = transactionRepository.findAllRealizedPlTransactions();

        assertSame(expected, result);
        verify(jdbcTemplate).query(
                eq("SELECT * FROM transactions WHERE type = 'SELL' AND realized_pl IS NOT NULL"),
                any(TransactionRowMapper.class));
    }
}
