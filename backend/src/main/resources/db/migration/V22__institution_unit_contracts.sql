-- Ta'lim muassasasi va harbiy qism o'rtasidagi shartnoma: o'qituvchi faqat shartnomasi bor muassasadan qo'shiladi.
CREATE TABLE institution_units (
    institution_id   BIGINT NOT NULL REFERENCES education_institutions (id) ON DELETE CASCADE,
    military_unit_id BIGINT NOT NULL REFERENCES military_units (id),
    PRIMARY KEY (institution_id, military_unit_id)
);

-- Mavjud o'qituvchilar va guruhlar orqali allaqachon ishlayotgan juftliklar shartnomali deb hisoblanadi.
INSERT INTO institution_units (institution_id, military_unit_id)
SELECT DISTINCT institution_id, military_unit_id FROM teachers
UNION
SELECT DISTINCT institution_id, military_unit_id FROM study_groups WHERE institution_id IS NOT NULL;
