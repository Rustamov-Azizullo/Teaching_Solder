export type SettingInputKind = 'select' | 'time' | 'number' | 'text';
export type SettingGroupKey = 'survey' | 'lesson' | 'employment' | 'retention' | 'other';

export type SettingField = { key: string; kind: SettingInputKind };

/** Sozlama guruhlari: yangi kalit qo'shish uchun shu ro'yxatni to'ldirish yetarli. */
export const SETTING_GROUPS: { group: Exclude<SettingGroupKey, 'other'>; fields: SettingField[] }[] = [
  { group: 'survey', fields: [{ key: 'survey.futurePlan.mode', kind: 'select' }] },
  {
    group: 'lesson',
    fields: [
      { key: 'lesson.defaultStart', kind: 'time' },
      { key: 'lesson.defaultEnd', kind: 'time' },
      { key: 'lesson.defaultAcademicHours', kind: 'number' },
    ],
  },
  { group: 'employment', fields: [{ key: 'employment.monthsBeforeEnd', kind: 'number' }] },
  { group: 'retention', fields: [{ key: 'retention.years', kind: 'number' }] },
];

export const TIME_PATTERN = /^([01]\d|2[0-3]):[0-5]\d$/;
