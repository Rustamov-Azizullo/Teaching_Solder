import { useCallback, useMemo, useState } from 'react';
import type { AbsenceReason, AttendanceEntryInput, AttendanceStatus, DraftEntry, RosterEntry } from '../types';

type Draft = Map<number, DraftEntry>;

/** Mashg'ulot hali kiritilmagan bo'lsa, hamma "keldi" deb boshlanadi (tez kiritish uchun). */
function buildDraft(roster: RosterEntry[]): Draft {
  return new Map(
    roster.map((entry) => [entry.soldierId, { status: entry.status ?? 'PRESENT', reason: entry.reason ?? undefined }]),
  );
}

/**
 * Davomat qoralamasi. Boshlang'ich qiymat faqat bir marta olinadi: forma `key` orqali yangi mashg'ulot yoki
 * saqlangan davomat kelganda qayta yaratiladi, shuning uchun fondagi yangilanish kiritilgan belgilarni o'chirmaydi.
 */
export function useAttendanceDraft(roster: RosterEntry[]) {
  const [draft, setDraft] = useState<Draft>(() => buildDraft(roster));

  const setStatus = useCallback((soldierId: number, status: AttendanceStatus) => {
    setDraft((current) => {
      const next = new Map(current);
      next.set(soldierId, { status, reason: status === 'PRESENT' ? undefined : current.get(soldierId)?.reason });
      return next;
    });
  }, []);

  const setReason = useCallback((soldierId: number, reason: AbsenceReason) => {
    setDraft((current) => new Map(current).set(soldierId, { status: 'ABSENT', reason }));
  }, []);

  const markAllPresent = useCallback(() => {
    setDraft((current) => new Map([...current.keys()].map((id) => [id, { status: 'PRESENT' as const }])));
  }, []);

  const entries: AttendanceEntryInput[] = useMemo(
    () => [...draft].map(([soldierId, entry]) => ({ soldierId, status: entry.status, reason: entry.reason })),
    [draft],
  );
  const presentCount = entries.filter((entry) => entry.status === 'PRESENT').length;
  const firstMissingReasonId = entries.find((entry) => entry.status === 'ABSENT' && !entry.reason)?.soldierId;

  return {
    draft: Object.fromEntries(draft) as Record<number, DraftEntry>,
    entries,
    presentCount,
    hasMissingReason: firstMissingReasonId !== undefined,
    firstMissingReasonId,
    setStatus,
    setReason,
    markAllPresent,
  };
}
