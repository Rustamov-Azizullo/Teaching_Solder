import type { AuthUser, PermissionKey, Role } from './types';

/**
 * Interfeysda tugma/menyuni ko'rsatish uchun. Haqiqiy tekshiruv serverda amalga oshiriladi.
 * Frontend capability kalitlari backenddagi `Permission` kalitlariga shu jadval orqali bog'lanadi.
 */
const capabilityPermissions = {
  admin: 'ADMIN',
  systemConfig: 'SYSTEM_CONFIG',
  dictionaryWrite: 'DICTIONARY_WRITE',
  unitDirections: 'UNIT_DIRECTIONS',
  soldierRead: 'SOLDIER_READ',
  soldierWrite: 'SOLDIER_WRITE',
  questionnaireRead: 'QUESTIONNAIRE_READ',
  questionnaireWrite: 'QUESTIONNAIRE_WRITE',
  groupRead: 'GROUP_READ',
  groupWrite: 'GROUP_WRITE',
  transfer: 'TRANSFER',
  attachmentWrite: 'ATTACHMENT_WRITE',
  resultRead: 'RESULT_READ',
  resultWrite: 'RESULT_WRITE',
  admissionRead: 'ADMISSION_READ',
  admissionWrite: 'ADMISSION_WRITE',
  employment: 'EMPLOYMENT',
  deadlineManage: 'DEADLINE_MANAGE',
  reports: 'REPORTS',
  leaderAssign: 'GROUP_LEADER_ASSIGN',
  dashboardVocational: 'DASHBOARD_VOCATIONAL',
  dashboardOtm: 'DASHBOARD_OTM',
  dashboardSurveys: 'DASHBOARD_SURVEYS',
} as const satisfies Record<string, PermissionKey>;

export type Capability = keyof typeof capabilityPermissions;

/** Backenddagi `Role.isAlwaysAllowed()`: bu rol har qanday ruxsat tekshiruvidan o'tadi (sozlanmaydi). */
const ALWAYS_ALLOWED_ROLES: readonly Role[] = ['MEGA_SUPER_ADMIN'];

/** Backenddagi `Access.PERMISSION_MANAGE`: ruxsatlarni boshqarish dinamik tizimdan tashqarida, statik rol tekshiruvi. */
const PERMISSION_MANAGER_ROLES: readonly Role[] = ['MEGA_SUPER_ADMIN', 'SUPER_ADMIN'];

export function can(user: AuthUser | null | undefined, capability: Capability): boolean {
  if (!user) return false;
  if (ALWAYS_ALLOWED_ROLES.includes(user.role)) return true;
  return user.permissions.includes(capabilityPermissions[capability]);
}

/** Berilgan imkoniyatlardan kamida bittasi bo'lsa `true`. */
export function canAny(user: AuthUser | null | undefined, capabilities: readonly Capability[]): boolean {
  return capabilities.some((capability) => can(user, capability));
}

export function isPermissionManager(user: AuthUser | null | undefined): boolean {
  return user !== null && user !== undefined && PERMISSION_MANAGER_ROLES.includes(user.role);
}

const DASHBOARD_CAPABILITIES: readonly Capability[] = ['dashboardVocational', 'dashboardOtm', 'dashboardSurveys'];

/** Admin/User: o'z doirasidagi qismlar ma'lumoti sahifasiga (`/catalog`) kirishi mumkin; super rollarda umumiy ma'lumotnoma bor. */
export function canOpenScopedCatalog(user: AuthUser | null | undefined): boolean {
  return (user?.role === 'ADMIN' || user?.role === 'USER') && canAny(user, DASHBOARD_CAPABILITIES);
}
