-- Mavjud bazaga yangi rol (MEGA_SUPER_ADMIN, SUPER_ADMIN, ADMIN, USER) demo hisoblarini qo'shish.
-- Sabab: DemoDataSeeder faqat bo'sh bazada ishlaydi (users.count() == 0), shuning uchun
-- bu hisoblar avvaldan mavjud bazalarda avtomatik yaratilmaydi.
--
-- Ishlatish (Postgres konteyneriga ulanib):
--   docker compose exec -T db psql -U postgres -d askar_talimi -f /dev/stdin < backend/add-superadmin-roles.sql
-- yoki psql orqali to'g'ridan-to'g'ri:
--   psql "postgresql://postgres:postgres@localhost:5432/askar_talimi" -f backend/add-superadmin-roles.sql
--
-- Parol barcha hisoblar uchun: Parol123!  (boshqa demo hisoblar bilan bir xil)

INSERT INTO app_users (username, password_hash, full_name, role, active, failed_attempts)
VALUES
    ('megasuperadmin', '$2b$10$BZGo8wXmM5MMLrN5I8.Z2O3soaiYF00U2w.k2VhW5flaEuCW6shm6', 'Mega SuperAdmin', 'MEGA_SUPER_ADMIN', TRUE, 0),
    ('superadmin',     '$2b$10$BZGo8wXmM5MMLrN5I8.Z2O3soaiYF00U2w.k2VhW5flaEuCW6shm6', 'SuperAdmin',      'SUPER_ADMIN',      TRUE, 0),
    ('adminuser',      '$2b$10$BZGo8wXmM5MMLrN5I8.Z2O3soaiYF00U2w.k2VhW5flaEuCW6shm6', 'Admin',           'ADMIN',            TRUE, 0),
    ('user',           '$2b$10$BZGo8wXmM5MMLrN5I8.Z2O3soaiYF00U2w.k2VhW5flaEuCW6shm6', 'User',            'USER',             TRUE, 0)
ON CONFLICT (username) DO NOTHING;
