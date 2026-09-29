-- Guruh kattasi buyruq raqami/sanasi o'rniga JShShIR bilan yuritiladi.
ALTER TABLE group_leaders ADD COLUMN pinfl VARCHAR(14);
ALTER TABLE study_groups DROP COLUMN leader_order_no;
ALTER TABLE study_groups DROP COLUMN leader_order_date;
