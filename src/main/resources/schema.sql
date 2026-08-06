-- Consolidated schema from V1 + V3 migrations
-- This file defines the final multi-user structure directly (no incremental migrations).

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS portfolio_snapshots;
DROP TABLE IF EXISTS milestones;
DROP TABLE IF EXISTS exchange_rates;
DROP TABLE IF EXISTS app_settings;
DROP TABLE IF EXISTS investments;
DROP TABLE IF EXISTS app_users;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    google_subject VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS app_settings (
    user_id BIGINT NOT NULL,
    setting_key VARCHAR(50) NOT NULL,
    setting_value VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, setting_key),
    CONSTRAINT fk_app_settings_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS exchange_rates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    rate_to_base DECIMAL(20,8) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_exchange_rates_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT uq_exchange_rates_user_currency UNIQUE (user_id, currency_code),
    KEY idx_exchange_rates_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS investments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL,
    symbol VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    country VARCHAR(50) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    quantity DECIMAL(20,6) NULL,
    avg_buy_price DECIMAL(20,6) NULL,
    current_price DECIMAL(20,6) NULL,
    invested_amount DECIMAL(20,6) NOT NULL DEFAULT 0,
    current_value DECIMAL(20,6) NOT NULL DEFAULT 0,
    previous_value DECIMAL(20,6) NULL,
    interest_rate DECIMAL(6,3) NULL,
    maturity_date DATE NULL,
    purchase_date DATE NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    notes VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_investments_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT chk_investment_type CHECK (type IN ('STOCK', 'ETF', 'FD', 'CASH')),
    CONSTRAINT chk_investment_status CHECK (status IN ('ACTIVE', 'CLOSED')),
    KEY idx_investments_user (user_id),
    KEY idx_investments_type (type),
    KEY idx_investments_status (status),
    KEY idx_investments_country (country)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    investment_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL,
    quantity DECIMAL(20,6) NULL,
    price DECIMAL(20,6) NULL,
    amount DECIMAL(20,6) NOT NULL,
    realized_pl DECIMAL(20,6) NULL,
    currency VARCHAR(3) NOT NULL,
    transaction_date DATE NOT NULL,
    notes VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_investment FOREIGN KEY (investment_id) REFERENCES investments (id) ON DELETE CASCADE,
    CONSTRAINT chk_transaction_type CHECK (type IN ('BUY', 'SELL', 'DEPOSIT', 'WITHDRAW', 'INTEREST')),
    KEY idx_transactions_investment (investment_id),
    KEY idx_transactions_date (transaction_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS portfolio_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    total_invested_base DECIMAL(20,6) NOT NULL,
    total_value_base DECIMAL(20,6) NOT NULL,
    realized_pl_base DECIMAL(20,6) NOT NULL DEFAULT 0,
    unrealized_pl_base DECIMAL(20,6) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_portfolio_snapshots_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT uq_portfolio_snapshot_user_date UNIQUE (user_id, snapshot_date),
    KEY idx_portfolio_snapshots_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS milestones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    threshold_value_base DECIMAL(20,2) NOT NULL,
    comparison_label VARCHAR(150) NOT NULL,
    achieved BOOLEAN NOT NULL DEFAULT FALSE,
    achieved_date DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_milestones_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT uq_milestone_user_threshold UNIQUE (user_id, threshold_value_base),
    KEY idx_milestones_user (user_id),
    KEY idx_milestones_achieved (achieved)
) ENGINE=InnoDB;
