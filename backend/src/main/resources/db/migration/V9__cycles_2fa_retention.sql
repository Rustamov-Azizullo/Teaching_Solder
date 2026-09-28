CREATE TABLE cycles (
    cycle_year INT PRIMARY KEY,
    status    VARCHAR(10) NOT NULL,
    opened_at TIMESTAMP   NOT NULL,
    closed_at TIMESTAMP
);

ALTER TABLE app_users ADD COLUMN totp_secret VARCHAR(64);
ALTER TABLE app_users ADD COLUMN totp_enabled BOOLEAN NOT NULL DEFAULT FALSE;

INSERT INTO app_settings (setting_key, setting_value) VALUES
    ('retention.years', '5'),
    ('security.2fa.required', 'false');
