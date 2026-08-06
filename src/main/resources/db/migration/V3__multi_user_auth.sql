CREATE TABLE app_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    google_subject VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT INTO app_users (google_subject, email, display_name)
VALUES ('legacy-single-user', 'legacy@example.local', 'Legacy User');

ALTER TABLE exchange_rates
    ADD COLUMN user_id BIGINT NULL AFTER id,
    ADD CONSTRAINT fk_exchange_rates_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE;

UPDATE exchange_rates
SET user_id = (SELECT id FROM app_users WHERE google_subject = 'legacy-single-user')
WHERE user_id IS NULL;

ALTER TABLE exchange_rates
    DROP INDEX currency_code,
    ADD CONSTRAINT uq_exchange_rates_user_currency UNIQUE (user_id, currency_code);

ALTER TABLE exchange_rates
    MODIFY user_id BIGINT NOT NULL;

CREATE INDEX idx_exchange_rates_user ON exchange_rates (user_id);

ALTER TABLE app_settings
    ADD COLUMN user_id BIGINT NULL AFTER setting_key,
    ADD CONSTRAINT fk_app_settings_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE;

UPDATE app_settings
SET user_id = (SELECT id FROM app_users WHERE google_subject = 'legacy-single-user')
WHERE user_id IS NULL;

ALTER TABLE app_settings
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (user_id, setting_key);

ALTER TABLE app_settings
    MODIFY user_id BIGINT NOT NULL;

ALTER TABLE investments
    ADD COLUMN user_id BIGINT NULL AFTER id,
    ADD CONSTRAINT fk_investments_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE;

UPDATE investments
SET user_id = (SELECT id FROM app_users WHERE google_subject = 'legacy-single-user')
WHERE user_id IS NULL;

ALTER TABLE investments
    MODIFY user_id BIGINT NOT NULL;

CREATE INDEX idx_investments_user ON investments (user_id);

ALTER TABLE milestones
    ADD COLUMN user_id BIGINT NULL AFTER id,
    ADD CONSTRAINT fk_milestones_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE;

UPDATE milestones
SET user_id = (SELECT id FROM app_users WHERE google_subject = 'legacy-single-user')
WHERE user_id IS NULL;

ALTER TABLE milestones
    MODIFY user_id BIGINT NOT NULL;

ALTER TABLE milestones
    DROP INDEX uq_milestone_threshold,
    ADD CONSTRAINT uq_milestone_user_threshold UNIQUE (user_id, threshold_value_base);

CREATE INDEX idx_milestones_user ON milestones (user_id);

ALTER TABLE portfolio_snapshots
    ADD COLUMN user_id BIGINT NULL AFTER id,
    ADD CONSTRAINT fk_portfolio_snapshots_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE;

UPDATE portfolio_snapshots
SET user_id = (SELECT id FROM app_users WHERE google_subject = 'legacy-single-user')
WHERE user_id IS NULL;

ALTER TABLE portfolio_snapshots
    MODIFY user_id BIGINT NOT NULL;

ALTER TABLE portfolio_snapshots
    DROP INDEX snapshot_date,
    ADD CONSTRAINT uq_portfolio_snapshot_user_date UNIQUE (user_id, snapshot_date);

CREATE INDEX idx_portfolio_snapshots_user ON portfolio_snapshots (user_id);
