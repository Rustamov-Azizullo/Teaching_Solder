-- Biriktirishlar (muassasa -> harbiy qism) moduli olib tashlandi.
DELETE FROM role_permissions WHERE permission IN ('ASSIGNMENT_READ', 'ASSIGNMENT_PROPOSE', 'ASSIGNMENT_DECIDE');
DELETE FROM user_permissions WHERE permission IN ('ASSIGNMENT_READ', 'ASSIGNMENT_PROPOSE', 'ASSIGNMENT_DECIDE');
DELETE FROM attachments WHERE owner_type = 'ASSIGNMENT';
DROP TABLE assignments;
