import type { PermissionKey, Role } from '@/features/auth';

/** Matritsada sozlanadigan rollar; Mega SuperAdmin har doim barcha ruxsatlarga ega va jadvalda ko'rsatilmaydi. */
export type ConfigurableRole = Extract<Role, 'SUPER_ADMIN' | 'ADMIN' | 'USER'>;

/** `GET/PUT /api/role-permissions` elementi (backenddagi `RolePermissionEntry`). */
export type RolePermissionEntry = { role: ConfigurableRole; permission: PermissionKey; granted: boolean };

export type MatrixCellKey = `${ConfigurableRole}:${PermissionKey}`;

export type RolePermissionRow = { permission: PermissionKey; granted: Record<ConfigurableRole, boolean> };
