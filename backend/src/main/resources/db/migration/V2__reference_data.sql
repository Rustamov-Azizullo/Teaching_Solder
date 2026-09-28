-- Boshlang'ich ma'lumotnomalar (TT 6.1, M1). Viloyat/tumanlar to'liq reyestri XLSX orqali yuklanadi (TT 7.2).

INSERT INTO app_settings (setting_key, setting_value) VALUES
    ('survey.futurePlan.mode', 'MULTIPLE'),
    ('attendance.editHours', '24'),
    ('lesson.defaultStart', '15:00'),
    ('lesson.defaultEnd', '17:25'),
    ('lesson.defaultAcademicHours', '3');

INSERT INTO regions (name) VALUES
    ('Qoraqalpog''iston Respublikasi'), ('Andijon viloyati'), ('Buxoro viloyati'), ('Farg''ona viloyati'),
    ('Jizzax viloyati'), ('Xorazm viloyati'), ('Namangan viloyati'), ('Navoiy viloyati'),
    ('Qashqadaryo viloyati'), ('Samarqand viloyati'), ('Sirdaryo viloyati'), ('Surxondaryo viloyati'),
    ('Toshkent viloyati'), ('Toshkent shahri');

INSERT INTO districts (region_id, name)
SELECT r.id, d.name FROM regions r JOIN (
    SELECT 'Toshkent shahri' AS region, 'Yunusobod tumani' AS name UNION ALL
    SELECT 'Toshkent shahri', 'Chilonzor tumani' UNION ALL
    SELECT 'Toshkent shahri', 'Mirzo Ulug''bek tumani' UNION ALL
    SELECT 'Samarqand viloyati', 'Samarqand shahri' UNION ALL
    SELECT 'Samarqand viloyati', 'Urgut tumani' UNION ALL
    SELECT 'Farg''ona viloyati', 'Farg''ona shahri' UNION ALL
    SELECT 'Farg''ona viloyati', 'Qo''qon shahri' UNION ALL
    SELECT 'Andijon viloyati', 'Andijon shahri' UNION ALL
    SELECT 'Buxoro viloyati', 'Buxoro shahri' UNION ALL
    SELECT 'Xorazm viloyati', 'Urganch shahri' UNION ALL
    SELECT 'Namangan viloyati', 'Namangan shahri' UNION ALL
    SELECT 'Navoiy viloyati', 'Navoiy shahri' UNION ALL
    SELECT 'Qashqadaryo viloyati', 'Qarshi shahri' UNION ALL
    SELECT 'Surxondaryo viloyati', 'Termiz shahri' UNION ALL
    SELECT 'Jizzax viloyati', 'Jizzax shahri' UNION ALL
    SELECT 'Sirdaryo viloyati', 'Guliston shahri' UNION ALL
    SELECT 'Toshkent viloyati', 'Zangiota tumani' UNION ALL
    SELECT 'Qoraqalpog''iston Respublikasi', 'Nukus shahri'
) d ON d.region = r.name;

-- Kasb yo'nalishlari (anketa 14-savol)
INSERT INTO dictionary_items (type, code, name, sort_order) VALUES
    ('PROFESSION_DIRECTION', 'AUTO', 'Avtomobillarga texnik xizmat ko''rsatish va mashinist (avtokran, ekskavator)', 1),
    ('PROFESSION_DIRECTION', 'SERVICE', 'Xizmat ko''rsatish sohasi (sartarosh, oshpaz, qandolatchi, novvoy)', 2),
    ('PROFESSION_DIRECTION', 'CONSTRUCTION', 'Qurilish sohasi (suvoqchi, betonchi, g''isht teruvchi, plitkachi, kafel ishlari)', 3),
    ('PROFESSION_DIRECTION', 'PAINTING', 'Bo''yoqchilik va ta''mirlash (bo''yoqchi, bezakchi, gipsokarton)', 4),
    ('PROFESSION_DIRECTION', 'CARPENTRY', 'Duradgorlik (mebel yasash va ta''mirlash, pol yotqizish)', 5),
    ('PROFESSION_DIRECTION', 'FARMING', 'Dehqonchilik (issiqxona, limonchilik, bog''dorchilik)', 6),
    ('PROFESSION_DIRECTION', 'IT', 'Kompyuter, IT va dasturlash', 7),
    ('PROFESSION_DIRECTION', 'LANGUAGES', 'Til kurslari (ingliz, nemis, koreys)', 8),
    ('PROFESSION_DIRECTION', 'ELECTRO', 'Elektromontaj va elektronika', 9),
    ('PROFESSION_DIRECTION', 'BUSINESS', 'Buxgalteriya va tadbirkorlik (biznes, savdo)', 10),
    ('PROFESSION_DIRECTION', 'SERVICES', 'Servis xizmatlari (ofitsiant, santexnik, tokar)', 11),
    ('PROFESSION_DIRECTION', 'LIVESTOCK', 'Chorvachilik (baliqchilik, asalarichilik, quyonchilik)', 12),
    ('PROFESSION_DIRECTION', 'OTHER', 'Boshqa', 13);

-- Kelgusi rejalar (anketa 15-savol). OTM_ADMISSION kodi V bo'limni ochadi.
INSERT INTO dictionary_items (type, code, name, sort_order) VALUES
    ('FUTURE_PLAN', 'PRIVATE_JOB', 'Xususiy sektorda doimiy ishga joylashish', 1),
    ('FUTURE_PLAN', 'PUBLIC_JOB', 'Davlat sektorida doimiy ishga joylashish', 2),
    ('FUTURE_PLAN', 'BUSINESS', 'Tadbirkorlik', 3),
    ('FUTURE_PLAN', 'OTM_ADMISSION', 'Oliy ta''limga o''qishga kirish', 4),
    ('FUTURE_PLAN', 'ABROAD', 'Xorijda ishlash', 5),
    ('FUTURE_PLAN', 'PROFESSION', 'Kasb-hunarni egallash', 6),
    ('FUTURE_PLAN', 'CONTINUE_SERVICE', 'Harbiy xizmatni davom ettirish', 7),
    ('FUTURE_PLAN', 'FARMING', 'Dehqonchilik', 8),
    ('FUTURE_PLAN', 'CRAFTS', 'Hunarmandchilik', 9),
    ('FUTURE_PLAN', 'OTHER', 'Boshqa', 10);

-- Fanlar (dinamik; algoritmdagi boshlang'ich to'plam)
INSERT INTO dictionary_items (type, code, name, hours, sort_order) VALUES
    ('SUBJECT', 'MOTHER_TONGUE', 'Ona tili (o''zbek tili)', 120, 1),
    ('SUBJECT', 'MATH', 'Matematika', 180, 2),
    ('SUBJECT', 'UZ_HISTORY', 'O''zbekiston tarixi', 120, 3),
    ('SUBJECT', 'PHYSICS', 'Fizika', 120, 4),
    ('SUBJECT', 'CHEMISTRY', 'Kimyo', 120, 5),
    ('SUBJECT', 'BIOLOGY', 'Biologiya', 120, 6),
    ('SUBJECT', 'GEOGRAPHY', 'Geografiya', 90, 7),
    ('SUBJECT', 'HISTORY', 'Tarix', 90, 8),
    ('SUBJECT', 'FOREIGN_LANGUAGE', 'Chet tili', 120, 9);

-- Tasdiqlangan kasblar (namuna; qo'shma qaror asosida yangilanadi)
INSERT INTO dictionary_items (type, code, name, hours, sort_order) VALUES
    ('PROFESSION', 'COOK', 'Oshpaz', 480, 1),
    ('PROFESSION', 'BARBER', 'Sartarosh', 480, 2),
    ('PROFESSION', 'PLASTERER', 'Suvoqchi', 480, 3),
    ('PROFESSION', 'CARPENTER', 'Duradgor', 480, 4),
    ('PROFESSION', 'ELECTRICIAN', 'Elektromontajchi', 480, 5),
    ('PROFESSION', 'PROGRAMMER', 'Dasturchi (asoslar)', 480, 6),
    ('PROFESSION', 'DRIVER', 'Avtomobil ustasi', 480, 7);

INSERT INTO dictionary_items (type, code, name, sort_order) VALUES
    ('LANGUAGE', 'EN', 'Ingliz tili', 1), ('LANGUAGE', 'DE', 'Nemis tili', 2),
    ('LANGUAGE', 'KO', 'Koreys tili', 3), ('LANGUAGE', 'RU', 'Rus tili', 4),
    ('LANGUAGE', 'FR', 'Fransuz tili', 5), ('LANGUAGE', 'ZH', 'Xitoy tili', 6),
    ('LANGUAGE', 'AR', 'Arab tili', 7);

INSERT INTO dictionary_items (type, code, name, sort_order) VALUES
    ('KINSHIP', 'FATHER', 'Ota', 1), ('KINSHIP', 'MOTHER', 'Ona', 2),
    ('KINSHIP', 'BROTHER', 'Aka-uka', 3), ('KINSHIP', 'SISTER', 'Opa-singil', 4),
    ('KINSHIP', 'OTHER', 'Boshqa yaqin qarindosh', 5);

INSERT INTO dictionary_items (type, code, name, sort_order) VALUES
    ('AWARD_KIND', 'REP_SPORT', 'Respublika sport musobaqasi', 1),
    ('AWARD_KIND', 'REP_OLYMPIAD', 'Respublika fan olimpiadasi', 2),
    ('AWARD_KIND', 'INT_SPORT', 'Xalqaro sport musobaqasi', 3),
    ('AWARD_KIND', 'INT_OLYMPIAD', 'Xalqaro fan olimpiadasi', 4);
