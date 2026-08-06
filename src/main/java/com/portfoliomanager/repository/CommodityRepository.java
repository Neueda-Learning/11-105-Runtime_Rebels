package com.portfoliomanager.repository;

import com.portfoliomanager.mapper.CommodityRowMapper;
import com.portfoliomanager.model.Commodity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class CommodityRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CommodityRowMapper rowMapper = new CommodityRowMapper();

    public CommodityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Commodity> findAll() {
        return jdbcTemplate.query("SELECT * FROM commodities ORDER BY created_at DESC", rowMapper);
    }

    public Optional<Commodity> findById(Long id) {
        List<Commodity> rows = jdbcTemplate.query("SELECT * FROM commodities WHERE id = ?", rowMapper, id);
        return rows.stream().findFirst();
    }

    public Optional<Commodity> findByInvestmentId(Long investmentId) {
        List<Commodity> rows = jdbcTemplate.query("SELECT * FROM commodities WHERE investment_id = ?", rowMapper, investmentId);
        return rows.stream().findFirst();
    }

    public Commodity save(Commodity commodity) {
        String sql = """
                INSERT INTO commodities
                    (investment_id, commodity_name, commodity_type, market_exchange, country, currency,
                     quantity, purchase_price, current_price, purchase_date)
                VALUES (?,?,?,?,?,?,?,?,?,?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, commodity.getInvestmentId());
            ps.setString(2, commodity.getCommodityName());
            ps.setString(3, commodity.getCommodityType().name());
            ps.setString(4, commodity.getMarketExchange());
            ps.setString(5, commodity.getCountry());
            ps.setString(6, commodity.getCurrency());
            ps.setBigDecimal(7, commodity.getQuantity());
            ps.setBigDecimal(8, commodity.getPurchasePrice());
            ps.setBigDecimal(9, commodity.getCurrentPrice());
            ps.setObject(10, commodity.getPurchaseDate());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return findById(id).orElseThrow();
    }

    public Commodity update(Commodity commodity) {
        String sql = """
                UPDATE commodities
                SET commodity_name = ?, commodity_type = ?, market_exchange = ?, country = ?, currency = ?,
                    quantity = ?, purchase_price = ?, current_price = ?, purchase_date = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                commodity.getCommodityName(),
                commodity.getCommodityType().name(),
                commodity.getMarketExchange(),
                commodity.getCountry(),
                commodity.getCurrency(),
                commodity.getQuantity(),
                commodity.getPurchasePrice(),
                commodity.getCurrentPrice(),
                commodity.getPurchaseDate(),
                commodity.getId());
        return findById(commodity.getId()).orElseThrow();
    }

    public void upsertByInvestmentId(Commodity commodity) {
        Optional<Commodity> existing = findByInvestmentId(commodity.getInvestmentId());
        if (existing.isPresent()) {
            commodity.setId(existing.get().getId());
            update(commodity);
        } else {
            save(commodity);
        }
    }
}

