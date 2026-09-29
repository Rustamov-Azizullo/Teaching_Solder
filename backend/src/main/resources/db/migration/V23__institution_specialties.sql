-- Muassasada o'qitiladigan kasblar va fanlar (kasb yoki fan ma'lumotnomasi yozuvlari).
CREATE TABLE institution_specialties (
    institution_id     BIGINT NOT NULL REFERENCES education_institutions (id) ON DELETE CASCADE,
    dictionary_item_id BIGINT NOT NULL REFERENCES dictionary_items (id),
    PRIMARY KEY (institution_id, dictionary_item_id)
);

-- Mavjud o'qituvchilarning mutaxassisligi shu muassasada o'qitiladi deb hisoblanadi.
INSERT INTO institution_specialties (institution_id, dictionary_item_id)
SELECT DISTINCT institution_id, specialty_id FROM teachers WHERE specialty_id IS NOT NULL;
