import type { PageResponse } from '@/types/api';
import type { Role } from '@/features/auth';

export type UserRow = {
  id: number;
  username: string;
  fullName: string;
  role: Role;
  roleLabel: string;
  militaryDistrictId: number | null;
  militaryUnitId: number | null;
  militaryUnitName: string | null;
  active: boolean;
};

export type RoleOption = { code: Role; label: string; scopeLevel: 'REPUBLIC' | 'DISTRICT' | 'UNIT' };

export type CreateUserRequest = {
  username: string;
  password: string;
  fullName: string;
  role: Role;
  militaryDistrictId?: number;
  militaryUnitId?: number;
};

export type UpdateUserRequest = Omit<CreateUserRequest, 'username' | 'password'> & { active: boolean; newPassword?: string };

export type AuditLogRow = {
  id: number;
  at: string;
  username: string;
  action: string;
  entity: string;
  entityId: string | null;
  details: string | null;
};

export type IntegrationLogRow = {
  id: number; at: string; system: string; operation: string; reference: string | null; success: boolean;
  message: string | null; durationMs: number; actor: string;
};

export type CycleRow = { year: number; status: 'OPEN' | 'CLOSED'; openedAt: string; closedAt: string | null };

export type AuditPage = PageResponse<AuditLogRow>;
export type IntegrationPage = PageResponse<IntegrationLogRow>;
export type SettingsMap = Record<string, string>;
