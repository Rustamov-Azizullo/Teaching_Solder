import type { PermissionKey } from '@/features/auth';

export type PermissionGroup = 'system' | 'soldiers' | 'questionnaires' | 'assignments' | 'training' | 'admissions' | 'reporting';

/** Bo'limlar tartibi (jadvalda shu tartibda ko'rsatiladi). */
export const PERMISSION_GROUP_ORDER: readonly PermissionGroup[] = [
  'system', 'soldiers', 'questionnaires', 'assignments', 'training', 'admissions', 'reporting',
];

/** Har bir ruxsat aynan bitta bo'limga tegishli; yangi `PermissionKey` qo'shilsa, TypeScript shu yerda xato beradi. */
export const PERMISSION_GROUPS: Record<PermissionKey, PermissionGroup> = {
  ADMIN: 'system',
  SYSTEM_CONFIG: 'system',
  DICTIONARY_WRITE: 'system',
  UNIT_DIRECTIONS: 'system',
  SOLDIER_READ: 'soldiers',
  SOLDIER_WRITE: 'soldiers',
  TRANSFER: 'soldiers',
  ATTACHMENT_WRITE: 'soldiers',
  QUESTIONNAIRE_READ: 'questionnaires',
  QUESTIONNAIRE_WRITE: 'questionnaires',
  ASSIGNMENT_READ: 'assignments',
  ASSIGNMENT_PROPOSE: 'assignments',
  ASSIGNMENT_DECIDE: 'assignments',
  DEADLINE_MANAGE: 'assignments',
  GROUP_READ: 'training',
  GROUP_WRITE: 'training',
  GROUP_LEADER_ASSIGN: 'training',
  SCHEDULE_WRITE: 'training',
  SCHEDULE_TIME_OVERRIDE: 'training',
  RESULT_READ: 'training',
  RESULT_WRITE: 'training',
  ADMISSION_READ: 'admissions',
  ADMISSION_WRITE: 'admissions',
  EMPLOYMENT: 'admissions',
  REPORTS: 'reporting',
  DASHBOARD_VOCATIONAL: 'reporting',
  DASHBOARD_OTM: 'reporting',
  DASHBOARD_SURVEYS: 'reporting',
};

/** Qatorlarni bo'lim tartibiga keltiradi (bo'lim ichida katalog tartibi saqlanadi). */
export function sortByGroup<T extends { permission: PermissionKey }>(rows: T[]): T[] {
  const rank = (row: T) => PERMISSION_GROUP_ORDER.indexOf(PERMISSION_GROUPS[row.permission]);
  return rows.map((row, index) => ({ row, index })).sort((a, b) => rank(a.row) - rank(b.row) || a.index - b.index).map(({ row }) => row);
}
