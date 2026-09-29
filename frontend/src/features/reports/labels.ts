import type { ReportType } from './types';

export const reportTypeLabels: Record<ReportType, string> = {
  WEEKLY_UNIT_SUMMARY: 'Haftalik hisobot: harbiy qismlar kesimida (okruglar uchun)',
  COURSE_COMPLETION: 'Kurs yakuni hisoboti (HKTB uchun)',
  OTM_ADMISSIONS: 'OTMga qabul natijalari (HKTB uchun)',
  YEARLY_SUMMARY: 'Vazirlik miqyosidagi yillik umumlashma',
};

export const reportLabels = {
  title: 'Hisobotlar',
  subtitle: 'Yagona uslubdagi hisobotlar: sarlavha, davr, vakolat doirasi, sana va tuzuvchi bilan',
  type: 'Hisobot turi',
  xlsx: 'XLSX yuklab olish',
  pdf: 'PDF yuklab olish',
  ready: 'Hisobot tayyor',
} as const;
