import type { PermissionKey } from '@/features/auth';

/** Har bir ruxsat qaysi sahifa yoki bo'limni ochishi (jadvalda "Ochadigan sahifalar" ustuni). */
export const PERMISSION_PAGES: Record<PermissionKey, string> = {
  ADMIN: 'Foydalanuvchilar',
  SYSTEM_CONFIG: 'Sozlamalar, Audit jurnali, Integratsiya jurnali',
  DICTIONARY_WRITE: "Ma'lumotnomalar",
  UNIT_DIRECTIONS: "Ma'lumotnomalar → qism yo'nalishlari",
  SOLDIER_READ: "Askarlar, Bo'linmalar",
  SOLDIER_WRITE: 'Askarlar → yangi askar, import',
  QUESTIONNAIRE_READ: 'Askar anketasi',
  QUESTIONNAIRE_WRITE: 'Askar anketasi',
  ATTACHMENT_WRITE: 'Askar, kurs natijasi fayllari',
  DEADLINE_MANAGE: 'Muddatlar',
  RESULT_READ: 'Guruh → Kurs yakuni',
  RESULT_WRITE: 'Guruh → Kurs yakuni',
  ADMISSION_READ: 'OTMga qabul',
  ADMISSION_WRITE: 'OTMga qabul',
  EMPLOYMENT: "Bandlik ro'yxatlari",
  TRANSFER: "Askar kartasi → boshqa qismga o'tkazish",
  GROUP_READ: "Guruhlar, O'qituvchilar",
  GROUP_WRITE: "Guruhlar, O'qituvchilar, Muassasalar, Bo'linmalar",
  GROUP_LEADER_ASSIGN: 'Guruh formasi → guruh kattasi, Kurs yakuni',
  REPORTS: 'Hisobotlar',
  DASHBOARD_VOCATIONAL: 'Dashboard → Kasb kurslari',
  DASHBOARD_OTM: 'Dashboard → OTM tayyorlov',
  DASHBOARD_SURVEYS: "Dashboard → So'rovnoma natijalari",
};
