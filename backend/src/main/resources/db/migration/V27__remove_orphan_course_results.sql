-- V26 kasb kursi guruhidan chiqarilgan askarlarning kurs natijalari qolib ketgan edi: guruh a'zosi bo'lmagan
-- askarlarning natijalarini o'chiramiz.
DELETE FROM course_results
WHERE NOT EXISTS (
    SELECT 1
    FROM group_soldiers gs
    WHERE gs.group_id = course_results.group_id
      AND gs.soldier_id = course_results.soldier_id);
