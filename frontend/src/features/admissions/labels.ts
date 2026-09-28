import type { OnlineStatus, StudyForm } from './types';

export const studyFormLabels: Record<StudyForm, string> = { FULL_TIME: 'Kunduzgi', EVENING: 'Kechki', PART_TIME: 'Sirtqi', DISTANCE: 'Masofaviy' };
export const onlineStatusLabels: Record<OnlineStatus, string> = { NONE: "Yo'q", AGREED: 'Kelishilgan', STUDYING: "O'qiyapti" };

export const admissionLabels = {
  title: 'OTMga qabul',
  subtitle: 'Nomzodlar, BMBA holati, test natijalari va qabul',
  tabs: { candidates: 'Nomzodlar', reserve: "Zaxiraga bo'shatish ro'yxati" },
  sync: 'BMBA dan yangilash',
  synced: (r: { synced: number; notFound: number; failed: number }) => `Yangilandi: ${r.synced}, topilmadi: ${r.notFound}, xato: ${r.failed}`,
  columns: { name: 'F.I.Sh.', unit: 'Qism', bmba: 'BMBA', benefits: 'Imtiyoz', test: 'Test', score: 'Ball', admitted: 'Qabul', university: 'OTM', online: 'Onlayn', end: 'Xizmat tugashi' },
  edit: 'Tahrirlash',
  editTitle: 'Qabul ma\'lumotlari',
  fields: { bmba: "BMBAda ro'yxatdan o'tgan", benefits: 'Imtiyozlar yuklangan', test: 'Testda qatnashgan', score: 'Test bali', admitted: 'Qabul qilingan', university: 'OTM nomi', direction: "Ta'lim yo'nalishi", form: "Ta'lim shakli", online: "Onlayn o'qish" },
  saved: 'Saqlandi',
  funnel: 'OTMga qabul voronkasi',
  reserveHint: "Qabul qilingan va xizmati tez tugaydigan askarlar — muddatidan bir oy oldin zaxiraga bo'shatish uchun",
} as const;
