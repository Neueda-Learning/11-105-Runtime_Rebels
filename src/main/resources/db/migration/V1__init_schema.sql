-- =====================================================================================
-- Portfolio Manager - Initial schema (V1)
-- Covers the minimum fields needed for the first working version, as per the
-- customer's requirements gathered in the interview:
--   - multiple investment types: STOCK, ETF, FD, CASH
--   - multi country / multi currency support
--   - realized vs unrealized P/L tracking
--   - dashboard aggregates (via investments + transactions)
--   - daily wealth overview / performance history (portfolio_snapshots)
--   - wealth milestones
-- =====================================================================================

-- ---------------------------------------------------------------------------
-- app_settings: simple key/value store, e.g. the customer's base/preferred currency
-- ---------------------------------------------------------------------------
CREATE TABLE app_settings (
    setting_key   VARCHAR(50)  NOT NULL PRIMARY KEY,
    setting_value VARCHAR(100) NOT NULL,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- exchange_rates: rate to convert 1 unit of currency_code into the base currency.
-- Lets the customer view values in original currency AND their base currency.
-- ---------------------------------------------------------------------------
CREATE TABLE exchange_rates (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    currency_code VARCHAR(3)      NOT NULL UNIQUE,
    rate_to_base  DECIMAL(20,8)   NOT NULL,
    updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- investments: one row per holding / position.
-- quantity / avg_buy_price / current_price are used for STOCK & ETF.
-- invested_amount / current_value are the universal fields (in instrument currency)
-- that every investment type (including FD and CASH) populates, which keeps the
-- dashboard aggregation logic simple and uniform across types.
-- previous_value supports the "today's gain/loss" requirement.
-- ---------------------------------------------------------------------------
CREATE TABLE investments (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    type                  VARCHAR(10)     NOT NULL,               -- STOCK, ETF, FD, CASH
    symbol                VARCHAR(30)     NOT NULL,
    name                  VARCHAR(150)    NOT NULL,
    country               VARCHAR(50)     NOT NULL,                -- e.g. India, US, UK, Europe, China
    currency              VARCHAR(3)      NOT NULL,                -- ISO currency code, e.g. INR, USD, GBP, EUR, CNY

    quantity              DECIMAL(20,6)   NULL,                    -- units held (STOCK / ETF)
    avg_buy_price         DECIMAL(20,6)   NULL,                    -- average cost per unit (STOCK / ETF)
    current_price         DECIMAL(20,6)   NULL,                    -- latest market price per unit (STOCK / ETF)

    invested_amount       DECIMAL(20,6)   NOT NULL DEFAULT 0,      -- total cost basis, instrument currency
    current_value         DECIMAL(20,6)   NOT NULL DEFAULT 0,      -- current market value, instrument currency
    previous_value        DECIMAL(20,6)   NULL,                    -- value as of previous close/day - for today's P/L

    interest_rate         DECIMAL(6,3)    NULL,                    -- annual %, FD only
    maturity_date         DATE            NULL,                    -- FD only
    purchase_date         DATE            NULL,

    status                VARCHAR(10)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, CLOSED
    notes                 VARCHAR(255)    NULL,

    created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_investment_type CHECK (type IN ('STOCK','ETF','FD','CASH')),
    CONSTRAINT chk_investment_status CHECK (status IN ('ACTIVE','CLOSED'))
) ENGINE=InnoDB;

CREATE INDEX idx_investments_type ON investments (type);
CREATE INDEX idx_investments_status ON investments (status);
CREATE INDEX idx_investments_country ON investments (country);

-- ---------------------------------------------------------------------------
-- transactions: full history of buys/sells/deposits/withdrawals/interest.
-- SELL transactions capture realized_pl so realized vs unrealized P/L can be
-- reported separately, as required by the customer.
-- ---------------------------------------------------------------------------
CREATE TABLE transactions (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    investment_id     BIGINT          NOT NULL,
    type              VARCHAR(10)     NOT NULL,   -- BUY, SELL, DEPOSIT, WITHDRAW, INTEREST
    quantity          DECIMAL(20,6)   NULL,
    price             DECIMAL(20,6)   NULL,
    amount            DECIMAL(20,6)   NOT NULL,   -- total transaction amount, instrument currency
    realized_pl       DECIMAL(20,6)   NULL,       -- populated for SELL transactions
    currency          VARCHAR(3)      NOT NULL,
    transaction_date  DATE            NOT NULL,
    notes             VARCHAR(255)    NULL,
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transactions_investment FOREIGN KEY (investment_id)
        REFERENCES investments (id) ON DELETE CASCADE,
    CONSTRAINT chk_transaction_type CHECK (type IN ('BUY','SELL','DEPOSIT','WITHDRAW','INTEREST'))
) ENGINE=InnoDB;

CREATE INDEX idx_transactions_investment ON transactions (investment_id);
CREATE INDEX idx_transactions_date ON transactions (transaction_date);

-- ---------------------------------------------------------------------------
-- portfolio_snapshots: one row per day, used to power the performance chart
-- and the daily wealth overview history.
-- ---------------------------------------------------------------------------
CREATE TABLE portfolio_snapshots (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    snapshot_date         DATE            NOT NULL UNIQUE,
    total_invested_base   DECIMAL(20,6)   NOT NULL,
    total_value_base      DECIMAL(20,6)   NOT NULL,
    realized_pl_base      DECIMAL(20,6)   NOT NULL DEFAULT 0,
    unrealized_pl_base    DECIMAL(20,6)   NOT NULL DEFAULT 0,
    created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- milestones: wealth-related, feel-good milestones the customer asked for,
-- e.g. "Portfolio crossed the price of a luxury car".
-- ---------------------------------------------------------------------------
CREATE TABLE milestones (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                  VARCHAR(100)    NOT NULL,
    threshold_value_base  DECIMAL(20,2)   NOT NULL,
    comparison_label      VARCHAR(150)    NOT NULL,
    achieved              BOOLEAN         NOT NULL DEFAULT FALSE,
    achieved_date         DATE            NULL,
    created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_milestone_threshold UNIQUE (threshold_value_base)
) ENGINE=InnoDB;

CREATE INDEX idx_milestones_achieved ON milestones (achieved);
