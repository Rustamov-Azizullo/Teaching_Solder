import type { Role } from './types';

/**
 * Interfeysda tugma/menyuni ko'rsatish uchun. Haqiqiy tekshiruv serverda (RBAC) amalga oshiriladi;
 * bu jadval backenddagi `Access` konstantalari bilan mos turishi kerak.
 */
const ALL_UNIT_MANAGERS: Role[] = ['SYSTEM_ADMIN', 'UNIT_COMMANDER', 'UNIT_OPERATOR'];

export const capabilities = {
  admin: ['SYSTEM_ADMIN'],
  dictionaryWrite: ['SYSTEM_ADMIN', 'HKTB'],
  unitDirections: ['SYSTEM_ADMIN', 'HKTB', 'UNIT_COMMANDER', 'UNIT_OPERATOR'],
  soldierRead: [
    'SYSTEM_ADMIN', 'HKTB', 'DISTRICT_OFFICER', 'UNIT_COMMANDER', 'UNIT_OPERATOR',
    'PSYCHOLOGIST', 'GROUP_LEADER',
  ],
  soldierWrite: ['SYSTEM_ADMIN', 'UNIT_COMMANDER', 'UNIT_OPERATOR', 'PSYCHOLOGIST'],
  questionnaireRead: [
    'SYSTEM_ADMIN', 'HKTB', 'DISTRICT_OFFICER', 'UNIT_COMMANDER', 'UNIT_OPERATOR', 'PSYCHOLOGIST',
  ],
  questionnaireWrite: ['SYSTEM_ADMIN', 'PSYCHOLOGIST', 'UNIT_COMMANDER'],
  groupRead: [
    'SYSTEM_ADMIN', 'HKTB', 'JTB', 'TMIBB', 'DISTRICT_OFFICER', 'UNIT_COMMANDER', 'UNIT_OPERATOR',
    'COMBAT_TRAINING_DEPT', 'EDUCATION_DEPT', 'GROUP_LEADER', 'PSYCHOLOGIST',
  ],
  groupWrite: ALL_UNIT_MANAGERS,
  transfer: ['SYSTEM_ADMIN', 'UNIT_COMMANDER'],
  attachmentWrite: ['SYSTEM_ADMIN', 'UNIT_COMMANDER', 'UNIT_OPERATOR', 'PSYCHOLOGIST'],
  assignmentRead: ['SYSTEM_ADMIN', 'HKTB', 'DISTRICT_OFFICER', 'UNIT_COMMANDER', 'UNIT_OPERATOR'],
  assignmentPropose: ['SYSTEM_ADMIN', 'DISTRICT_OFFICER', 'UNIT_COMMANDER', 'UNIT_OPERATOR'],
  assignmentDecide: ['SYSTEM_ADMIN', 'HKTB'],
  resultRead: [
    'SYSTEM_ADMIN', 'HKTB', 'DISTRICT_OFFICER', 'UNIT_COMMANDER', 'UNIT_OPERATOR', 'COMBAT_TRAINING_DEPT', 'EDUCATION_DEPT',
  ],
  resultWrite: ['SYSTEM_ADMIN', 'UNIT_COMMANDER', 'UNIT_OPERATOR'],
  ktaCompare: ['SYSTEM_ADMIN', 'HKTB'],
  admissionRead: [
    'SYSTEM_ADMIN', 'HKTB', 'TMIBB', 'DISTRICT_OFFICER', 'UNIT_COMMANDER', 'UNIT_OPERATOR', 'EDUCATION_DEPT',
  ],
  admissionWrite: ['SYSTEM_ADMIN', 'UNIT_COMMANDER', 'UNIT_OPERATOR'],
  employment: ['SYSTEM_ADMIN', 'HKTB', 'DISTRICT_OFFICER', 'UNIT_COMMANDER', 'UNIT_OPERATOR'],
  deadlineManage: ['SYSTEM_ADMIN', 'HKTB'],
  reports: [
    'SYSTEM_ADMIN', 'HKTB', 'JTB', 'TMIBB', 'DISTRICT_OFFICER', 'UNIT_COMMANDER', 'UNIT_OPERATOR',
    'COMBAT_TRAINING_DEPT', 'EDUCATION_DEPT',
  ],
  leaderAssign: ['SYSTEM_ADMIN', 'UNIT_COMMANDER'],
  scheduleWrite: ['SYSTEM_ADMIN', 'JTB', 'UNIT_COMMANDER', 'UNIT_OPERATOR'],
  attendanceWrite: ['SYSTEM_ADMIN', 'UNIT_COMMANDER', 'GROUP_LEADER'],
  dashboardVocational: [
    'SYSTEM_ADMIN', 'HKTB', 'JTB', 'DISTRICT_OFFICER', 'UNIT_COMMANDER', 'UNIT_OPERATOR', 'COMBAT_TRAINING_DEPT',
  ],
  dashboardOtm: [
    'SYSTEM_ADMIN', 'HKTB', 'TMIBB', 'DISTRICT_OFFICER', 'UNIT_COMMANDER', 'UNIT_OPERATOR', 'EDUCATION_DEPT',
  ],
  dashboardSurveys: [
    'SYSTEM_ADMIN', 'HKTB', 'DISTRICT_OFFICER', 'UNIT_COMMANDER', 'UNIT_OPERATOR', 'PSYCHOLOGIST',
  ],
} satisfies Record<string, Role[]>;

export type Capability = keyof typeof capabilities;

export function can(role: Role | undefined, capability: Capability): boolean {
  if (!role) return false;
  return (capabilities[capability] as Role[]).includes(role);
}
