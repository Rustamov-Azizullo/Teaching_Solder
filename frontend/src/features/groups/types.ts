import type { NamedRef } from '@/types/api';

export type GroupType = 'VOCATIONAL' | 'OTM_PREP';
export type InstitutionType = 'TECHNICAL_SCHOOL' | 'SCHOOL' | 'TRAINING_CENTER';

export type GroupSummary = {
  id: number;
  name: string;
  type: GroupType;
  militaryUnitId: number;
  militaryUnitName: string;
  professionName: string | null;
  subjectNames: string[];
  startDate: string;
  endDate: string;
  memberCount: number;
  leaderName: string | null;
};

export type Teacher = {
  id: number;
  fullName: string;
  specialty: string;
  specialtyIds: number[];
  institutionId: number;
  institutionName: string;
};

/** `pinfl` — faqat askarlarni ko'rish huquqi bor foydalanuvchiga beriladi. */
export type Member = { id: number; pinfl: string | null; fullName: string };

export type Group = {
  id: number;
  name: string;
  type: GroupType;
  militaryUnitId: number;
  militaryUnitName: string;
  institution: NamedRef | null;
  profession: NamedRef | null;
  subjects: NamedRef[];
  startDate: string;
  endDate: string;
  classroom: string | null;
  leader: GroupLeaderInfo | null;
  members: Member[];
  teachers: Teacher[];
};

export type GroupRequest = {
  name: string;
  type: GroupType;
  militaryUnitId: number;
  institutionId?: number;
  professionId?: number;
  subjectIds?: number[];
  startDate: string;
  endDate: string;
  classroom?: string;
  leader?: GroupLeaderInput;
};

export type GroupLeaderInfo = {
  id: number;
  fullName: string;
  pinfl: string | null;
  militaryRank: string | null;
  phone: string | null;
};

/** Guruh shakllantirilayotganda kiritiladigan guruh kattasi; harbiy qism guruhdan aniqlanadi. */
export type GroupLeaderInput = { fullName: string; pinfl: string; militaryRank?: string; phone?: string };

export type TeacherRequest = {
  fullName: string;
  specialtyIds: number[];
  institutionId: number;
};

/** `professionIds`/`subjectIds` — muassasada o'qitiladigan kasblar va fanlar (ma'lumotnoma id lari). */
export type Institution = { id: number; type: InstitutionType; name: string; professionIds: number[]; subjectIds: number[] };
export type InstitutionRequest = { type: InstitutionType; name: string; professionIds?: number[]; subjectIds?: number[] };

/** Muassasaning harbiy qismga biriktirilishi (shartnoma). */
export type InstitutionContract = {
  institutionId: number; institutionName: string; institutionType: InstitutionType;
  unitId: number; unitName: string; districtName: string;
};
export type ContractRequest = { institutionId: number; unitId: number };
