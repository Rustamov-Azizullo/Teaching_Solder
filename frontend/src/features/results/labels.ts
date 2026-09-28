import type { CourseStatus } from './types';

export const courseStatusLabels: Record<CourseStatus, string> = {
  STUDIED: "O'qidi",
  EXAM_PASSED: "Imtihondan o'tdi",
  CERTIFIED: 'Sertifikat oldi',
  DROPPED: "O'qishni tugatmadi",
};

export const resultLabels = {
  title: 'Kurs yakuni va sertifikatlar',
  columns: { name: 'F.I.Sh.', status: 'Holat', grade: 'Baho', reason: 'Sabab', certNo: 'Sertifikat raqami', certDate: 'Sanasi', issuer: 'Bergan muassasa' },
  save: 'Natijalarni saqlash',
  approve: 'Kurs yakunini tasdiqlash',
  approved: (by: string | null) => `Kurs yakuni tasdiqlangan${by ? ` (${by})` : ''}`,
  saved: 'Natijalar saqlandi',
  approvedDone: 'Kurs yakuni tasdiqlandi',
  confirmApprove: "Tasdiqlangandan keyin natijalarni o'zgartirib bo'lmaydi. Tasdiqlaysizmi?",
  minutes: 'Imtihon qaydnomasi skani',
  onlyVocational: "Kurs natijalari faqat kasb kurslari uchun kiritiladi",
  kta: {
    title: 'KTA bilan solishtirish',
    subtitle: "Bizdagi sertifikatlar KTA ma'lumotlari bilan tekshiriladi",
    api: 'API orqali tekshirish',
    file: 'Zaxira: XLSX yuklash (JShShIR, sertifikat raqami)',
    ours: 'Bizda', theirs: 'KTA da', problem: 'Nomuvofiqlik', unit: 'Qism',
    none: 'Nomuvofiqliklar topilmadi',
    found: (n: number) => `Nomuvofiqliklar: ${n}`,
  },
} as const;
