import type { PageResponse } from '@/types/api';
import type { LocationLevel, PermissionKey, Role } from '@/features/auth';

export type UserRow = {
  id: number;
  username: string;
  fullName: string;
  role: Role;
  roleLabel: string;
  locationId: number | null;
  locationName: string | null;
  locationLevel: LocationLevel | null;
  active: boolean;
};

export type RoleOption = { code: Role; label: string; scopeLevel: LocationLevel };

/** `GET /api/locations` elementi (tekis ro'yxat; daraxt `parentId` orqali quriladi). */
export type Location = {
  id: number;
  parentId: number | null;
  level: LocationLevel;
  name: string;
  code: string | null;
  militaryDistrictId: number | null;
  militaryUnitId: number | null;
};

/** `POST/PUT /api/locations` tanasi; tahrirlashda faqat `name` va `code` e'tiborga olinadi. */
export type LocationInput = { name: string; code?: string; level?: LocationLevel; parentId?: number };

export type CreateUserRequest = {
  username: string;
  password: string;
  fullName: string;
  role: Role;
  /** Respublika rollari uchun berilmaydi; okrug roli uchun okrug, qism roli uchun qism hududi. */
  locationId?: number;
};

export type UpdateUserRequest = Omit<CreateUserRequest, 'username' | 'password'> & { active: boolean; newPassword?: string };

/** `GET /api/users/{id}/permissions`: `granted` — shaxsiy ruxsat, `grantedByRole` — rol orqali allaqachon bor. */
export type UserPermissionState = { permission: PermissionKey; granted: boolean; grantedByRole: boolean };

/** `PUT /api/users/{id}/permissions` elementi: faqat ko'rsatilgan ruxsatlar o'zgaradi. */
export type UserPermissionChange = { permission: PermissionKey; granted: boolean };

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
