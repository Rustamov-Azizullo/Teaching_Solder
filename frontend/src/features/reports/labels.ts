import type { ReportGroupType, ReportType } from './types';

export const reportTypeLabels: Record<ReportType, string> = {
  ATTENDANCE_DAILY: 'Kunlik davomat hisoboti',
  ATTENDANCE_WEEKLY: 'Haftalik umumlashma hisoboti',
  COURSE_COMPLETION: 'Kurs yakuni hisoboti (HKTB uchun)',
  OTM_ADMISSIONS: 'OTMga qabul natijalari (HKTB uchun)',
  YEARLY_SUMMARY: 'Vazirlik miqyosidagi yillik umumlashma',
};

export const reportGroupTypeLabels: Record<ReportGroupType, string> = { VOCATIONAL: 'Kasb kurslari', OTM_PREP: 'OTM tayyorlov kurslari' };

export const reportLabels = {
  title: 'Hisobotlar',
  subtitle: 'Yagona uslubdagi hisobotlar: sarlavha, davr, vakolat doirasi, sana va tuzuvchi bilan',
  type: 'Hisobot turi',
  direction: "Yo'nalish (davomat hisobotlari alohida)",
  period: 'Davr',
  date: 'Sana',
  xlsx: 'XLSX yuklab olish',
  pdf: 'PDF yuklab olish',
  ready: 'Hisobot tayyor',
} as const;
