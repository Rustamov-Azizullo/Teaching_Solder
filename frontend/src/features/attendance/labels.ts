import type { AbsenceReason, AttendanceStatus } from './types';

export const absenceReasonLabels: Record<AbsenceReason, string> = {
  DUTY: 'Navbatchilik',
  ILLNESS: 'Kasallik',
  SERVICE_TASK: 'Xizmat vazifasi',
  NO_REASON: 'Sababsiz',
};

export const statusLabels: Record<AttendanceStatus, string> = { PRESENT: 'Keldi', ABSENT: 'Kelmadi' };

export const attendanceLabels = {
  title: 'Davomat',
  todayTitle: "Bugungi mashg'ulotlar",
  todaySubtitle: "Davomat kiritish uchun mashg'ulotni tanlang",
  todayEmpty: "Bugun mashg'ulot rejalashtirilmagan",
  recorded: 'Davomat kiritilgan',
  notRecorded: 'Davomat kiritilmagan',
  enter: 'Davomat kiritish',
  view: "Ko'rish / tuzatish",
  allPresent: 'Barchasi keldi',
  teacherPresent: "O'qituvchi keldi",
  topic: "O'tilgan mavzu",
  reasonPlaceholder: 'Sababi',
  reasonMissing: 'Sabab kiriting',
  reasonRequired: "Kelmagan askarlar uchun sabab tanlang",
  summary: (present: number, total: number) => `Keldi: ${present} / ${total}`,
  saved: 'Davomat saqlandi',
  empty: "Guruhda askarlar yo'q",
  readOnly: 'Faqat ko\'rish rejimi',
} as const;
