-- Dars jadvali moduli olib tashlandi: ruxsatlar, sozlamalar va jadvallar.
DELETE FROM role_permissions WHERE permission IN ('SCHEDULE_WRITE', 'SCHEDULE_TIME_OVERRIDE');
DELETE FROM user_permissions WHERE permission IN ('SCHEDULE_WRITE', 'SCHEDULE_TIME_OVERRIDE');
DELETE FROM app_settings WHERE setting_key IN ('lesson.defaultStart', 'lesson.defaultEnd', 'lesson.defaultAcademicHours', 'attendance.editHours');
DROP TABLE attendances;
DROP TABLE lessons;
