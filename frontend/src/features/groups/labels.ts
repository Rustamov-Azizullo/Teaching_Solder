import type { GroupType, InstitutionType } from './types';

export const groupTypeLabels: Record<GroupType, string> = {
  VOCATIONAL: 'Kasb kursi',
  OTM_PREP: 'OTM tayyorlov kursi',
};

export const institutionTypeLabels: Record<InstitutionType, string> = {
  TECHNICAL_SCHOOL: 'Texnikum',
  SCHOOL: 'Maktab',
  TRAINING_CENTER: "O'quv markazi",
};

export const groupLabels = {
  listTitle: 'Guruhlar',
  listSubtitle: 'Kasb kurslari va OTM tayyorlov guruhlari',
  create: 'Yangi guruh',
  createTitle: 'Yangi guruh yaratish',
  editTitle: 'Guruhni tahrirlash',
  columns: {
    name: 'Guruh', type: 'Turi', unit: 'Harbiy qism', curriculum: 'Kasb / fanlar', period: 'Davri',
    members: 'Askarlar', leader: 'Guruh kattasi',
  },
  fields: {
    name: 'Guruh nomi',
    type: 'Guruh turi',
    unit: 'Harbiy qism',
    institution: 'Biriktirilgan muassasa',
    profession: 'Kasb',
    subjects: 'Fanlar',
    startDate: 'Boshlanish sanasi',
    endDate: 'Tugash sanasi',
    classroom: 'Sinf (xona)',
    leader: 'Guruh kattasi',
    leaderOrder: 'Buyruq',
    orderNo: 'Buyruq raqami',
    orderDate: 'Buyruq sanasi',
  },
  vocationalNote: 'Kasb kursi 6 oydan oshmasligi kerak',
  tabs: { info: "Ma'lumot", members: 'Askarlar', teachers: "O'qituvchilar", schedule: 'Dars jadvali', results: 'Kurs yakuni' },
  noLeader: 'Tayinlanmagan',
  assignLeader: 'Guruh kattasini tayinlash',
  leaderUser: 'Xodim',
  membersHint: 'Guruhga qo\'shiladigan askarlarni tanlang (shu qismdagi askarlar)',
  teachersHint: "Guruhga biriktiriladigan o'qituvchilar",
  saved: 'Saqlandi',
  teachers: {
    title: "O'qituvchilar va muassasalar",
    subtitle: "O'qituvchilar faqat ma'lumot yozuvi — ular tizimga kirmaydi",
    add: "O'qituvchi qo'shish",
    addInstitution: "Muassasa qo'shish",
    specialty: 'Mutaxassisligi',
    institution: 'Tashkiloti',
    accessOrder: 'Kirish buyrug\'i',
    accessUntil: 'Ruxsat muddati',
    expiring: 'Muddati tugayapti',
    institutionType: 'Muassasa turi',
    institutionName: 'Muassasa nomi',
  },
} as const;
