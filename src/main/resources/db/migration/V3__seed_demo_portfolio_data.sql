-- =====================================================================================
-- Demo portfolio data (V3)
-- Purpose: showcase-ready sample dataset for dashboard, holdings, transactions, and trends.
-- Safe for normal backend flow: valid enum values, valid FK references, realistic value ranges.
-- =====================================================================================

-- -----------------------------
-- 1) Demo investments
-- -----------------------------
INSERT INTO investments
(type, symbol, name, country, currency, quantity, avg_buy_price, current_price,
 invested_amount, current_value, previous_value, interest_rate, maturity_date,
 purchase_date, status, notes)
VALUES
('STOCK', 'AAPL', 'Apple Inc.', 'US', 'USD', 20.000000, 170.000000, 195.000000,
 3400.000000, 3900.000000, 3820.000000, NULL, NULL, '2025-01-10', 'ACTIVE', 'demo-seed-v3'),
('STOCK', 'MSFT', 'Microsoft Corp.', 'US', 'USD', 12.000000, 330.000000, 420.000000,
 3960.000000, 5040.000000, 4920.000000, NULL, NULL, '2025-02-02', 'ACTIVE', 'demo-seed-v3'),
('STOCK', 'NVDA', 'NVIDIA Corp.', 'US', 'USD', 18.000000, 105.000000, 128.000000,
 1890.000000, 2304.000000, 2250.000000, NULL, NULL, '2025-03-14', 'ACTIVE', 'demo-seed-v3'),
('ETF', 'QQQ', 'Invesco QQQ Trust', 'US', 'USD', 14.000000, 380.000000, 460.000000,
 5320.000000, 6440.000000, 6360.000000, NULL, NULL, '2024-11-21', 'ACTIVE', 'demo-seed-v3'),
('ETF', 'VOO', 'Vanguard S&P 500 ETF', 'US', 'USD', 8.000000, 430.000000, 515.000000,
 3440.000000, 4120.000000, 4050.000000, NULL, NULL, '2024-12-06', 'ACTIVE', 'demo-seed-v3'),
('STOCK', 'RELIANCE', 'Reliance Industries', 'India', 'INR', 50.000000, 2500.000000, 2980.000000,
 125000.000000, 149000.000000, 147000.000000, NULL, NULL, '2024-09-18', 'ACTIVE', 'demo-seed-v3'),
('STOCK', 'TCS', 'Tata Consultancy Services', 'India', 'INR', 28.000000, 3450.000000, 4200.000000,
 96600.000000, 117600.000000, 116100.000000, NULL, NULL, '2024-10-08', 'ACTIVE', 'demo-seed-v3'),
('ETF', 'NIFTYBEES', 'Nippon India ETF Nifty BeES', 'India', 'INR', 280.000000, 235.000000, 275.000000,
 65800.000000, 77000.000000, 76450.000000, NULL, NULL, '2024-08-02', 'ACTIVE', 'demo-seed-v3'),
('FD', 'HDFCFD1', 'HDFC 2Y Fixed Deposit', 'India', 'INR', NULL, NULL, NULL,
 500000.000000, 542500.000000, 541000.000000, 8.500, '2027-07-15', '2025-07-15', 'ACTIVE', 'demo-seed-v3'),
('FD', 'SBIFD1', 'SBI 3Y Fixed Deposit', 'India', 'INR', NULL, NULL, NULL,
 300000.000000, 336000.000000, 334800.000000, 8.000, '2028-05-01', '2025-05-01', 'ACTIVE', 'demo-seed-v3'),
('CASH', 'EMERG_CASH', 'Emergency Cash Buffer', 'India', 'INR', NULL, NULL, NULL,
 200000.000000, 200000.000000, 200000.000000, NULL, NULL, '2024-01-01', 'ACTIVE', 'demo-seed-v3'),
('CASH', 'TRAVEL_FUND', 'Travel Cash Reserve', 'US', 'USD', NULL, NULL, NULL,
 2500.000000, 2500.000000, 2500.000000, NULL, NULL, '2025-04-01', 'ACTIVE', 'demo-seed-v3'),
('STOCK', 'BABA', 'Alibaba Group', 'China', 'CNY', 40.000000, 88.000000, 76.000000,
 3520.000000, 3040.000000, 3100.000000, NULL, NULL, '2024-06-20', 'CLOSED', 'demo-seed-v3'),
('ETF', 'SXRV', 'iShares Nasdaq 100 UCITS', 'Europe', 'EUR', 22.000000, 740.000000, 810.000000,
 16280.000000, 17820.000000, 17690.000000, NULL, NULL, '2024-03-11', 'ACTIVE', 'demo-seed-v3');

-- -----------------------------
-- 1b) Bulk demo investments (300 rows)
-- -----------------------------
INSERT INTO investments
(type, symbol, name, country, currency, quantity, avg_buy_price, current_price,
 invested_amount, current_value, previous_value, interest_rate, maturity_date,
 purchase_date, status, notes)
SELECT
    CASE WHEN s.n % 3 = 0 THEN 'ETF' ELSE 'STOCK' END AS type,
    CONCAT('DINV', LPAD(s.n, 3, '0')) AS symbol,
    CONCAT('Demo Holding ', LPAD(s.n, 3, '0')) AS name,
    ELT((s.n % 5) + 1, 'US', 'India', 'UK', 'Europe', 'China') AS country,
    ELT((s.n % 5) + 1, 'USD', 'INR', 'GBP', 'EUR', 'CNY') AS currency,
    s.qty,
    s.avg_price,
    ROUND(s.avg_price * (1 + (((s.n % 11) - 5) / 50.0)), 6) AS current_price,
    ROUND(s.qty * s.avg_price, 6) AS invested_amount,
    ROUND(s.qty * ROUND(s.avg_price * (1 + (((s.n % 11) - 5) / 50.0)), 6), 6) AS current_value,
    ROUND(
        ROUND(s.qty * ROUND(s.avg_price * (1 + (((s.n % 11) - 5) / 50.0)), 6), 6) - ((s.n % 7) * 12.500000),
        6
    ) AS previous_value,
    NULL AS interest_rate,
    NULL AS maturity_date,
    DATE_SUB(CURDATE(), INTERVAL (s.n + 40) DAY) AS purchase_date,
    CASE WHEN s.n % 10 = 0 THEN 'CLOSED' ELSE 'ACTIVE' END AS status,
    'demo-seed-v3-bulk' AS notes
FROM (
    SELECT
        n,
        ROUND(5 + ((n % 40) * 1.250000), 6) AS qty,
        ROUND(20 + ((n % 90) * 3.500000), 6) AS avg_price
    FROM (
        SELECT (h.d * 100 + t.d * 10 + o.d) AS n
        FROM (
            SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
        ) h
        CROSS JOIN (
            SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
        ) t
        CROSS JOIN (
            SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
        ) o
    ) numbers
) s
WHERE s.n BETWEEN 1 AND 300
    AND NOT EXISTS (
    SELECT 1
    FROM investments i
    WHERE i.symbol = CONCAT('DINV', LPAD(s.n, 3, '0'))
);

-- -----------------------------
-- 2) Resolve IDs for FK inserts
-- -----------------------------
SET @inv_aapl      := (SELECT id FROM investments WHERE symbol='AAPL' AND notes='demo-seed-v3' ORDER BY id DESC LIMIT 1);
SET @inv_msft      := (SELECT id FROM investments WHERE symbol='MSFT' AND notes='demo-seed-v3' ORDER BY id DESC LIMIT 1);
SET @inv_nvda      := (SELECT id FROM investments WHERE symbol='NVDA' AND notes='demo-seed-v3' ORDER BY id DESC LIMIT 1);
SET @inv_qqq       := (SELECT id FROM investments WHERE symbol='QQQ' AND notes='demo-seed-v3' ORDER BY id DESC LIMIT 1);
SET @inv_voo       := (SELECT id FROM investments WHERE symbol='VOO' AND notes='demo-seed-v3' ORDER BY id DESC LIMIT 1);
SET @inv_reliance  := (SELECT id FROM investments WHERE symbol='RELIANCE' AND notes='demo-seed-v3' ORDER BY id DESC LIMIT 1);
SET @inv_tcs       := (SELECT id FROM investments WHERE symbol='TCS' AND notes='demo-seed-v3' ORDER BY id DESC LIMIT 1);
SET @inv_niftybees := (SELECT id FROM investments WHERE symbol='NIFTYBEES' AND notes='demo-seed-v3' ORDER BY id DESC LIMIT 1);
SET @inv_hdfcfd1   := (SELECT id FROM investments WHERE symbol='HDFCFD1' AND notes='demo-seed-v3' ORDER BY id DESC LIMIT 1);
SET @inv_sbifd1    := (SELECT id FROM investments WHERE symbol='SBIFD1' AND notes='demo-seed-v3' ORDER BY id DESC LIMIT 1);
SET @inv_emergcash := (SELECT id FROM investments WHERE symbol='EMERG_CASH' AND notes='demo-seed-v3' ORDER BY id DESC LIMIT 1);
SET @inv_travelfnd := (SELECT id FROM investments WHERE symbol='TRAVEL_FUND' AND notes='demo-seed-v3' ORDER BY id DESC LIMIT 1);
SET @inv_baba      := (SELECT id FROM investments WHERE symbol='BABA' AND notes='demo-seed-v3' ORDER BY id DESC LIMIT 1);
SET @inv_sxrv      := (SELECT id FROM investments WHERE symbol='SXRV' AND notes='demo-seed-v3' ORDER BY id DESC LIMIT 1);

-- -----------------------------
-- 3) Demo transactions
-- -----------------------------
INSERT INTO transactions
(investment_id, type, quantity, price, amount, realized_pl, currency, transaction_date, notes)
VALUES
(@inv_aapl, 'BUY',   15.000000, 160.000000, 2400.000000, NULL,      'USD', '2025-01-10', 'demo-seed-v3'),
(@inv_aapl, 'BUY',    5.000000, 200.000000, 1000.000000, NULL,      'USD', '2025-03-10', 'demo-seed-v3'),

(@inv_msft, 'BUY',   12.000000, 330.000000, 3960.000000, NULL,      'USD', '2025-02-02', 'demo-seed-v3'),

(@inv_nvda, 'BUY',   10.000000,  90.000000,  900.000000, NULL,      'USD', '2025-03-14', 'demo-seed-v3'),
(@inv_nvda, 'BUY',    8.000000, 123.750000,  990.000000, NULL,      'USD', '2025-04-14', 'demo-seed-v3'),

(@inv_qqq,  'BUY',    9.000000, 360.000000, 3240.000000, NULL,      'USD', '2024-11-21', 'demo-seed-v3'),
(@inv_qqq,  'BUY',    5.000000, 416.000000, 2080.000000, NULL,      'USD', '2025-02-12', 'demo-seed-v3'),

(@inv_voo,  'BUY',    8.000000, 430.000000, 3440.000000, NULL,      'USD', '2024-12-06', 'demo-seed-v3'),

(@inv_reliance, 'BUY', 30.000000, 2300.000000, 69000.000000, NULL,  'INR', '2024-09-18', 'demo-seed-v3'),
(@inv_reliance, 'BUY', 20.000000, 2800.000000, 56000.000000, NULL,  'INR', '2024-11-04', 'demo-seed-v3'),

(@inv_tcs,  'BUY',   18.000000, 3200.000000, 57600.000000, NULL,    'INR', '2024-10-08', 'demo-seed-v3'),
(@inv_tcs,  'BUY',   10.000000, 3900.000000, 39000.000000, NULL,    'INR', '2025-01-22', 'demo-seed-v3'),

(@inv_niftybees, 'BUY', 200.000000, 220.000000, 44000.000000, NULL, 'INR', '2024-08-02', 'demo-seed-v3'),
(@inv_niftybees, 'BUY',  80.000000, 272.500000, 21800.000000, NULL, 'INR', '2025-02-15', 'demo-seed-v3'),

(@inv_hdfcfd1, 'DEPOSIT', NULL, NULL, 500000.000000, NULL,          'INR', '2025-07-15', 'demo-seed-v3'),
(@inv_hdfcfd1, 'INTEREST', NULL, NULL, 21250.000000, NULL,          'INR', '2026-01-15', 'demo-seed-v3'),
(@inv_hdfcfd1, 'INTEREST', NULL, NULL, 21250.000000, NULL,          'INR', '2026-07-15', 'demo-seed-v3'),

(@inv_sbifd1, 'DEPOSIT', NULL, NULL, 300000.000000, NULL,           'INR', '2025-05-01', 'demo-seed-v3'),
(@inv_sbifd1, 'INTEREST', NULL, NULL, 12000.000000, NULL,           'INR', '2025-11-01', 'demo-seed-v3'),
(@inv_sbifd1, 'INTEREST', NULL, NULL, 12000.000000, NULL,           'INR', '2026-05-01', 'demo-seed-v3'),

(@inv_emergcash, 'DEPOSIT', NULL, NULL, 200000.000000, NULL,        'INR', '2024-01-01', 'demo-seed-v3'),

(@inv_travelfnd, 'DEPOSIT', NULL, NULL, 1500.000000, NULL,          'USD', '2025-04-01', 'demo-seed-v3'),
(@inv_travelfnd, 'DEPOSIT', NULL, NULL, 1200.000000, NULL,          'USD', '2025-05-01', 'demo-seed-v3'),
(@inv_travelfnd, 'WITHDRAW', NULL, NULL, 200.000000, NULL,          'USD', '2025-06-12', 'demo-seed-v3'),

(@inv_baba, 'BUY',   40.000000, 88.000000, 3520.000000, NULL,       'CNY', '2024-06-20', 'demo-seed-v3'),
(@inv_baba, 'SELL',  40.000000, 76.000000, 3040.000000, -480.000000,'CNY', '2025-02-10', 'demo-seed-v3'),

(@inv_sxrv, 'BUY',   14.000000, 700.000000, 9800.000000, NULL,      'EUR', '2024-03-11', 'demo-seed-v3'),
(@inv_sxrv, 'BUY',    8.000000, 810.000000, 6480.000000, NULL,      'EUR', '2025-03-21', 'demo-seed-v3');

-- -----------------------------
-- 4) Longer chart history for dashboard (120 days)
-- -----------------------------
INSERT INTO portfolio_snapshots
(snapshot_date, total_invested_base, total_value_base, realized_pl_base, unrealized_pl_base)
SELECT
    DATE_SUB(CURDATE(), INTERVAL (119 - s.n) DAY) AS snapshot_date,
    ROUND(2400000 + (s.n * 15500), 6) AS total_invested_base,
    ROUND(2465000 + (s.n * 16120) + ((s.n % 7) * 2200), 6) AS total_value_base,
    ROUND(85000 + (s.n * 520), 6) AS realized_pl_base,
    ROUND((2465000 + (s.n * 16120) + ((s.n % 7) * 2200)) - (2400000 + (s.n * 15500)), 6) AS unrealized_pl_base
FROM (
    SELECT (t.d * 10 + o.d) AS n
    FROM (
        SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) t
    CROSS JOIN (
        SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) o
) s
WHERE s.n < 120
    AND NOT EXISTS (
    SELECT 1
    FROM portfolio_snapshots ps
    WHERE ps.snapshot_date = DATE_SUB(CURDATE(), INTERVAL (119 - s.n) DAY)
);

-- -----------------------------
-- 5) Extra milestones for better demo narrative
-- -----------------------------
INSERT INTO milestones (name, threshold_value_base, comparison_label)
VALUES
('International Vacation Milestone', 1500000.00, 'Your portfolio can now fund a premium international vacation!'),
('Home Down Payment Milestone', 2500000.00, 'Your portfolio value now rivals a strong home down payment.')
ON DUPLICATE KEY UPDATE
name = VALUES(name),
comparison_label = VALUES(comparison_label);
