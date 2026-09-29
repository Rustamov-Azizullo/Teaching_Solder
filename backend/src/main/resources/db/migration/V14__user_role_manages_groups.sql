-- Qism darajasidagi User roli o'qituvchilar, guruhlar va guruh kattalarini boshqara oladi.
-- Ruxsat rol huquqlari sahifasidan oldin berilgan bo'lishi mumkin, shuning uchun mavjud yozuv o'tkazib yuboriladi.
INSERT INTO role_permissions (role, permission)
SELECT 'USER', p.permission
FROM (SELECT 'GROUP_WRITE' AS permission UNION ALL SELECT 'GROUP_LEADER_ASSIGN') p
WHERE NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role = 'USER' AND rp.permission = p.permission);
