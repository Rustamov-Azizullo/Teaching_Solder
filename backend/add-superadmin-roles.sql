-- Mavjud bazaga 4 ta rol (MEGA_SUPER_ADMIN, SUPER_ADMIN, ADMIN, USER) demo hisoblarini qo'shish.
-- Sabab: DemoDataSeeder faqat bo'sh bazada ishlaydi (users.count() == 0), shuning uchun
-- bu hisoblar avvaldan mavjud bazalarda avtomatik yaratilmaydi.
--
-- Talab: V11 migratsiyasi (locations, role_permissions, user_permissions) qo'llangan bo'lishi kerak —
-- ilovani bir marta ishga tushirsangiz, Flyway uni avtomatik qo'llaydi.
--
-- Hududga biriktirish (app_users.location_id):
--   * megasuperadmin, superadmin — vazirlik darajasi, hududsiz (NULL);
--   * adminuser (ADMIN, okrug darajasi) — bazadagi birinchi harbiy okrug hududi;
--   * user (USER, qism darajasi) — bazadagi birinchi harbiy qism hududi.
-- Hududga biriktirilmagan ADMIN/USER hech qanday ma'lumotni ko'rmaydi, shuning uchun oldingi versiyadagi
-- skript bilan hududsiz yaratilgan adminuser/user hisoblari ham quyida biriktiriladi.
--
-- Ishlatish (Postgres konteyneriga ulanib):
--   docker compose exec -T db psql -U postgres -d askar_talimi -f /dev/stdin < backend/add-superadmin-roles.sql
-- yoki psql orqali to'g'ridan-to'g'ri:
--   psql "postgresql://postgres:postgres@localhost:5432/askar_talimi" -f backend/add-superadmin-roles.sql
--
-- Parol barcha hisoblar uchun: Parol123!  (boshqa demo hisoblar bilan bir xil)

INSERT INTO app_users (username, password_hash, full_name, role, location_id, active, failed_attempts)
VALUES
    ('megasuperadmin', '$2b$10$BZGo8wXmM5MMLrN5I8.Z2O3soaiYF00U2w.k2VhW5flaEuCW6shm6', 'Mega SuperAdmin', 'MEGA_SUPER_ADMIN',
     NULL, TRUE, 0),
    ('superadmin',     '$2b$10$BZGo8wXmM5MMLrN5I8.Z2O3soaiYF00U2w.k2VhW5flaEuCW6shm6', 'SuperAdmin',      'SUPER_ADMIN',
     NULL, TRUE, 0),
    ('adminuser',      '$2b$10$BZGo8wXmM5MMLrN5I8.Z2O3soaiYF00U2w.k2VhW5flaEuCW6shm6', 'Admin',           'ADMIN',
     (SELECT id FROM locations WHERE level = 'DISTRICT' ORDER BY id LIMIT 1), TRUE, 0),
    ('user',           '$2b$10$BZGo8wXmM5MMLrN5I8.Z2O3soaiYF00U2w.k2VhW5flaEuCW6shm6', 'User',            'USER',
     (SELECT id FROM locations WHERE level = 'UNIT' ORDER BY id LIMIT 1), TRUE, 0)
ON CONFLICT (username) DO NOTHING;

UPDATE app_users
SET location_id = (SELECT id FROM locations WHERE level = 'DISTRICT' ORDER BY id LIMIT 1)
WHERE username = 'adminuser' AND role = 'ADMIN' AND location_id IS NULL;

UPDATE app_users
SET location_id = (SELECT id FROM locations WHERE level = 'UNIT' ORDER BY id LIMIT 1)
WHERE username = 'user' AND role = 'USER' AND location_id IS NULL;
