DELETE FROM app_settings WHERE setting_key = 'security.2fa.required';
ALTER TABLE app_users DROP COLUMN totp_secret;
ALTER TABLE app_users DROP COLUMN totp_enabled;
