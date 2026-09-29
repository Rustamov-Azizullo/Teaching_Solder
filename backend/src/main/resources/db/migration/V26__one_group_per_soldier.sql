-- Askar faqat bitta guruhda bo'la oladi. Kasb va OTM guruhida bir vaqtda turgan askarlar
-- OTM guruhida qoldiriladi, kasb kursi guruhidan chiqariladi.
DELETE FROM group_soldiers
WHERE group_id IN (SELECT id FROM study_groups WHERE type = 'VOCATIONAL')
  AND soldier_id IN (
      SELECT gs.soldier_id
      FROM group_soldiers gs
      WHERE gs.group_id IN (SELECT id FROM study_groups WHERE type = 'OTM_PREP'));
