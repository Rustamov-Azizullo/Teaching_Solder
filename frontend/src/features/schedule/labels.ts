import type { LessonKind, LessonStatus } from './types';

export const lessonKindLabels: Record<LessonKind, string> = { THEORY: 'Nazariy', PRACTICAL: 'Amaliy' };

export const lessonStatusLabels: Record<LessonStatus, string> = {
  PLANNED: 'Rejalashtirilgan',
  HELD: "O'tkazilgan",
  CANCELLED: 'Bekor qilingan',
};

export const scheduleLabels = {
  title: 'Dars jadvali',
  standardTime: 'Standart vaqt: 15:00–17:25 (3 o\'quv soati)',
  columns: { date: 'Sana', time: 'Vaqt', topic: 'Mavzu', kind: 'Turi', status: 'Holat', hours: 'Soat' },
  generate: 'Jadvalni avtomatik yaratish',
  generateHint: 'Dushanba–juma kunlari uchun standart vaqt bilan mashg\'ulotlar yaratiladi',
  generated: (count: number) => `${count} ta mashg'ulot yaratildi`,
  add: "Mashg'ulot qo'shish",
  cancel: 'Bekor qilish',
  cancelReason: 'Bekor qilish sababi (bayram, dala mashg\'uloti va h.k.)',
  cancelled: "Mashg'ulot bekor qilindi",
  topic: 'Mavzu',
  kind: 'Turi',
  changeReason: 'Vaqt o\'zgartirilsa — sababi',
  today: {
    title: "Bugungi mashg'ulotlar",
    empty: "Bugun mashg'ulot rejalashtirilmagan",
  },
} as const;
