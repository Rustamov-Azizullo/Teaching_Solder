import type { Role } from '@/features/auth';
import type { ReportType, WeeklyMode } from './types';

export const reportTypeLabels: Record<ReportType, string> = {
  WEEKLY_UNIT_SUMMARY: 'Haftalik hisobot',
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

/** Haftalik hisobot ko'rinishlari: rolga qarab kesim va tanlanadigan ob'ekt nomi o'zgaradi. */
export const weeklyViewLabels: Record<Role, Partial<Record<WeeklyMode, string>>> = {
  MEGA_SUPER_ADMIN: { BREAKDOWN: 'Okruglar kesimida', TOTAL: 'Umumiy hisobot', DISTRICT: 'Okrugni tanlash' },
  SUPER_ADMIN: { BREAKDOWN: 'Okruglar kesimida', TOTAL: 'Umumiy hisobot', DISTRICT: 'Okrugni tanlash' },
  ADMIN: { BREAKDOWN: 'Harbiy qismlar kesimida', TOTAL: 'Umumiy hisobot', UNIT: 'Harbiy qismni tanlash' },
  USER: { TOTAL: 'Umumiy hisobot', SUBDIVISION: "Bo'linmani tanlash" },
};

export const weeklyTargetLabels = {
  DISTRICT: { field: 'Okrug', placeholder: 'Okrugni tanlang' },
  UNIT: { field: 'Harbiy qism', placeholder: 'Harbiy qismni tanlang' },
  SUBDIVISION: { field: "Bo'linma", placeholder: "Bo'linmani tanlang" },
} as const;

export const weeklyLabels = { view: "Hisobot ko'rinishi" } as const;
