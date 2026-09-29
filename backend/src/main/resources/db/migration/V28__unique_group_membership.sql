-- Askar faqat bitta guruhda bo'la oladi: qoida bazada ham ta'minlanadi (parallel so'rovlar buzmasligi uchun).
-- Bir nechta guruhda turgan askarlar eng birinchi (id si eng kichik) guruhda qoldiriladi.
DELETE FROM course_results
WHERE EXISTS (
    SELECT 1
    FROM group_soldiers keep
    WHERE keep.soldier_id = course_results.soldier_id
      AND keep.group_id < course_results.group_id)
  AND NOT EXISTS (
    SELECT 1
    FROM study_groups g
    WHERE g.id = course_results.group_id
      AND g.course_approved_at IS NOT NULL);

DELETE FROM group_soldiers
WHERE EXISTS (
    SELECT 1
    FROM (SELECT soldier_id, group_id FROM group_soldiers) keep
    WHERE keep.soldier_id = group_soldiers.soldier_id
      AND keep.group_id < group_soldiers.group_id);

CREATE UNIQUE INDEX ux_group_soldiers_soldier ON group_soldiers (soldier_id);
