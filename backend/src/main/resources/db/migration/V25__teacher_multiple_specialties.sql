-- O'qituvchining bir nechta mutaxassisligi (kasb yoki fan) bo'lishi mumkin.
CREATE TABLE teacher_specialties (
    teacher_id         BIGINT NOT NULL REFERENCES teachers (id) ON DELETE CASCADE,
    dictionary_item_id BIGINT NOT NULL REFERENCES dictionary_items (id),
    PRIMARY KEY (teacher_id, dictionary_item_id)
);

INSERT INTO teacher_specialties (teacher_id, dictionary_item_id)
SELECT id, specialty_id FROM teachers WHERE specialty_id IS NOT NULL;

ALTER TABLE teachers DROP COLUMN specialty_id;
