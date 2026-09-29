-- O'qituvchi mutaxassisligi endi ma'lumotnomadan (kasb yoki fan) tanlanadi.
-- `specialty` matni nomning nusxasi sifatida qoladi (ro'yxatlar va eski yozuvlar uchun).
ALTER TABLE teachers ADD COLUMN specialty_id BIGINT REFERENCES dictionary_items (id);

UPDATE teachers t
SET specialty_id = (SELECT d.id FROM dictionary_items d
                    WHERE d.type IN ('PROFESSION', 'SUBJECT') AND lower(d.name) = lower(t.specialty)
                    ORDER BY d.id LIMIT 1);
