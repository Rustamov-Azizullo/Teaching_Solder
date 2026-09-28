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
  institutionId: number;
  institutionName: string;
  militaryUnitId: number;
  accessOrderNo: string | null;
  accessValidUntil: string | null;
  accessExpiringSoon: boolean;
};

export type Member = { id: number; pinfl: string; fullName: string };

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
  leader: NamedRef | null;
  leaderOrderNo: string | null;
  leaderOrderDate: string | null;
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
};

export type LeaderRequest = { userId: number; orderNo: string; orderDate: string };

export type TeacherRequest = {
  fullName: string;
  specialty: string;
  institutionId: number;
  militaryUnitId: number;
  accessOrderNo?: string;
  accessValidUntil?: string;
};

export type Institution = { id: number; type: InstitutionType; name: string };
export type InstitutionRequest = { type: InstitutionType; name: string };
export type LeaderOption = { id: number; fullName: string };
