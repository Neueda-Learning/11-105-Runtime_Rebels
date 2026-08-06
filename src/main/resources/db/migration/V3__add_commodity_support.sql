-- Adds COMMODITY as an investment type and stores commodity-specific metadata.

ALTER TABLE investments DROP CHECK chk_investment_type;

ALTER TABLE investments
    ADD CONSTRAINT chk_investment_type CHECK (type IN ('STOCK','ETF','FD','CASH','COMMODITY'));

CREATE TABLE commodities (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    investment_id     BIGINT         NOT NULL UNIQUE,
    commodity_name    VARCHAR(150)   NOT NULL,
    commodity_type    VARCHAR(30)    NOT NULL,
    market_exchange   VARCHAR(50)    NOT NULL,
    country           VARCHAR(50)    NOT NULL,
    currency          VARCHAR(3)     NOT NULL,
    quantity          DECIMAL(20,6)  NOT NULL,
    purchase_price    DECIMAL(20,6)  NOT NULL,
    current_price     DECIMAL(20,6)  NOT NULL,
    purchase_date     DATE           NOT NULL,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_commodities_investment FOREIGN KEY (investment_id)
        REFERENCES investments(id) ON DELETE CASCADE,
    CONSTRAINT chk_commodity_type CHECK (commodity_type IN ('GOLD','SILVER','CRUDE_OIL','NATURAL_GAS','COPPER','PLATINUM','OTHER'))
) ENGINE=InnoDB;

CREATE INDEX idx_commodities_type ON commodities (commodity_type);
CREATE INDEX idx_commodities_market ON commodities (market_exchange);

