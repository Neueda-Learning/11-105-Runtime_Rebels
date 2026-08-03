-- =====================================================================================
-- Seed data: sensible defaults so the API is usable immediately after startup.
-- All of this can be changed later through the REST API (agile - easy to adjust).
-- =====================================================================================

-- Base/preferred currency the customer views their consolidated portfolio in.
INSERT INTO app_settings (setting_key, setting_value) VALUES ('base_currency', 'INR');

-- Starter exchange rates (1 unit of currency_code = rate_to_base units of base currency).
-- These are illustrative defaults - update via PUT /api/exchange-rates/{currencyCode}.
INSERT INTO exchange_rates (currency_code, rate_to_base) VALUES
    ('INR', 1.00000000),
    ('USD', 87.00000000),
    ('GBP', 110.00000000),
    ('EUR', 95.00000000),
    ('CNY', 12.00000000);

-- A few example wealth milestones (in base currency, INR by default).
INSERT INTO milestones (name, threshold_value_base, comparison_label) VALUES
    ('First Lakh', 100000.00, 'Your first Lakh! A great start to your wealth journey.'),
    ('Hatchback Milestone', 800000.00, 'Your portfolio is now worth a brand new hatchback car!'),
    ('Sedan Milestone', 2000000.00, 'Your portfolio could now buy a premium sedan!'),
    ('Luxury Car Milestone', 5000000.00, 'Your portfolio has crossed the price of a luxury car!'),
    ('First Crore', 10000000.00, 'You have crossed your first Crore! A huge wealth milestone.');
