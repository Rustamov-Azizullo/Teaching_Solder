import type { AssignmentStatus, Direction } from './types';

export const assignmentStatusLabels: Record<AssignmentStatus, string> = {
  PROPOSED: 'Taklif',
  UNDER_REVIEW: "Ko'rib chiqilmoqda",
  APPROVED: 'Tasdiqlangan',
  REJECTED: 'Rad etilgan',
};

export const directionLabels: Record<Direction, string> = { VOCATIONAL: 'Kasb kursi', OTM_PREP: 'OTM tayyorlov' };

export const assignmentLabels = {
  title: 'Biriktirishlar va shartnomalar',
  subtitle: "Texnikum, maktab va o'quv markazlarini harbiy qismlarga biriktirish",
  propose: 'Biriktirish taklifi',
  columns: { unit: 'Harbiy qism', institution: 'Muassasa', direction: "Yo'nalish", status: 'Holat', basis: 'Asos hujjat', contract: 'Shartnoma' },
  review: "Ko'rib chiqishga olish",
  decide: 'Qaror chiqarish',
  contract: 'Shartnoma va qo\'shma reja',
  files: 'Shartnoma va qo\'shma reja skanlari',
  fields: {
    unit: 'Harbiy qism', institution: 'Ta\'lim muassasasi', direction: "Yo'nalish", note: 'Izoh', approve: 'Tasdiqlash',
    basis: "Asos hujjat (qo'shma qaror / buyruq)", validity: 'Amal qilish muddati', contractNo: 'Shartnoma raqami',
    contractDate: 'Shartnoma sanasi', jointPlan: "Qo'shma reja",
  },
  done: 'Saqlandi',
} as const;
