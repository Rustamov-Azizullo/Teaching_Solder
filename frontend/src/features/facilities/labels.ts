import type { FacilityCondition, FacilityKind } from './types';

export const facilityKindLabels: Record<FacilityKind, string> = {
  CLASSROOM: 'Sinf', WORKSHOP: 'Ustaxona / amaliy joy', TRAINING_AREA: "O'quv maydoni", OTHER: 'Boshqa',
};

export const facilityConditionLabels: Record<FacilityCondition, string> = {
  GOOD: 'A\'lo', SATISFACTORY: 'Qoniqarli', POOR: 'Yomon', UNFIT: 'Yaroqsiz',
};

export const facilityLabels = {
  title: "O'quv-moddiy baza (xatlov)",
  subtitle: "Sinflar, o'quv joylari, jihozlar va ularning holati",
  add: "Xatlov yozuvi qo'shish",
  editTitle: 'Xatlov yozuvini tahrirlash',
  columns: { name: 'Nomi', kind: 'Turi', capacity: "Sig'im", condition: 'Holati', unit: 'Harbiy qism', shortages: 'Yetishmovchilik' },
  fields: {
    equipment: 'Jihozlar', shortages: 'Yetishmovchiliklar', surveyDate: 'Xatlov sanasi', suitable: 'Yaroqli kasb / fanlar',
    capacity: "Sig'im (kishi)",
  },
  act: 'Xatlov dalolatnomasi',
  saved: 'Saqlandi',
  deleted: "O'chirildi",
  confirmDelete: "O'chirilsinmi?",
} as const;
