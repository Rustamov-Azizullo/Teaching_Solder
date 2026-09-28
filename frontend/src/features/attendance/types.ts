import type { Lesson } from '@/features/schedule';

export type AttendanceStatus = 'PRESENT' | 'ABSENT';
export type AbsenceReason = 'DUTY' | 'ILLNESS' | 'SERVICE_TASK' | 'NO_REASON';

export type RosterEntry = {
  soldierId: number;
  fullName: string;
  status: AttendanceStatus | null;
  reason: AbsenceReason | null;
};

export type AttendanceSheet = {
  lesson: Lesson;
  roster: RosterEntry[];
  editable: boolean;
  readOnlyReason: string | null;
};

export type AttendanceEntryInput = { soldierId: number; status: AttendanceStatus; reason?: AbsenceReason };

export type AttendanceRequest = { teacherPresent: boolean; topic?: string; entries: AttendanceEntryInput[] };

export type DraftEntry = { status: AttendanceStatus; reason?: AbsenceReason };
