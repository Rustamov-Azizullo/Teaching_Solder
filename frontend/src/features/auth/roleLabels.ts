import type { Role } from './types';

export const roleLabels: Record<Role, string> = {
  MEGA_SUPER_ADMIN: 'Mega SuperAdmin',
  SUPER_ADMIN: 'SuperAdmin',
  ADMIN: 'Admin',
  USER: 'User',
  SYSTEM_ADMIN: 'Tizim administratori',
  HKTB: 'MV HKTB xodimi',
  JTB: 'MV JTB xodimi',
  TMIBB: 'TMIBB / MBMM xodimi',
  DISTRICT_OFFICER: "Harbiy okrug mas'uli",
  UNIT_COMMANDER: "Harbiy qism qo'mondoni",
  UNIT_OPERATOR: "Qism mas'ul xodimi (operator)",
  COMBAT_TRAINING_DEPT: "Qism jangovar tayyorgarlik bo'limi",
  EDUCATION_DEPT: "Qism tarbiyaviy ishlar bo'limi",
  GROUP_LEADER: 'Guruh kattasi',
  PSYCHOLOGIST: 'Harbiy psixolog (sotsiolog)',
};
