import type { PermissionKey, Role } from '@/features/auth';

/** Matritsada sozlanadigan rollar; SuperAdmin/Mega SuperAdmin har doim barcha ruxsatlarga ega. */
export type ConfigurableRole = Extract<Role, 'ADMIN' | 'USER'>;

/** `GET/PUT /api/role-permissions` elementi (backenddagi `RolePermissionEntry`). */
export type RolePermissionEntry = { role: ConfigurableRole; permission: PermissionKey; granted: boolean };

export type MatrixCellKey = `${ConfigurableRole}:${PermissionKey}`;

export type RolePermissionRow = { permission: PermissionKey; granted: Record<ConfigurableRole, boolean> };
