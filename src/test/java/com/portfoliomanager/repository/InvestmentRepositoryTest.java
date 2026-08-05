package com.portfoliomanager.repository;

import com.portfoliomanager.mapper.InvestmentRowMapper;
import com.portfoliomanager.model.Investment;
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
class InvestmentRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private InvestmentRepository investmentRepository;

    @Test
    void findAllActive_usesActiveFilterQuery() {
        List<Investment> expected = List.of(Investment.builder().id(1L).build());

        when(jdbcTemplate.query(eq("SELECT * FROM investments WHERE status = 'ACTIVE'"),
                any(InvestmentRowMapper.class)))
                .thenReturn(expected);

        List<Investment> result = investmentRepository.findAllActive();

        assertSame(expected, result);
        verify(jdbcTemplate).query(eq("SELECT * FROM investments WHERE status = 'ACTIVE'"),
                any(InvestmentRowMapper.class));
    }
}
