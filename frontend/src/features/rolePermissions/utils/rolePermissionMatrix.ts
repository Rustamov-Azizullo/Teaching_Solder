import type { PermissionKey, Role } from '@/features/auth';
import type { ConfigurableRole, MatrixCellKey, RolePermissionEntry, RolePermissionRow } from '../types';

export const CONFIGURABLE_ROLES: readonly ConfigurableRole[] = ['SUPER_ADMIN', 'ADMIN', 'USER'];

/** SuperAdmin ruxsatlarini faqat Mega SuperAdmin o'zgartira oladi (backenddagi `Role.isEditableBy`). */
export function canEditRole(editor: Role | undefined, role: ConfigurableRole): boolean {
  return role !== 'SUPER_ADMIN' || editor === 'MEGA_SUPER_ADMIN';
}

const NOTHING_GRANTED: Record<ConfigurableRole, boolean> = { SUPER_ADMIN: false, ADMIN: false, USER: false };

export function matrixCellKey(role: ConfigurableRole, permission: PermissionKey): MatrixCellKey {
  return `${role}:${permission}`;
}

/** Tekis ro'yxatni ruxsat bo'yicha qatorlarga yig'adi (backend katalogi tartibi saqlanadi). */
export function buildMatrixRows(entries: RolePermissionEntry[]): RolePermissionRow[] {
  const rows = new Map<PermissionKey, RolePermissionRow>();
  for (const entry of entries) {
    const row = rows.get(entry.permission) ?? { permission: entry.permission, granted: NOTHING_GRANTED };
    rows.set(entry.permission, { ...row, granted: { ...row.granted, [entry.role]: entry.granted } });
  }
  return [...rows.values()];
}

/** Server holatidan farq qiladigan kataklarni `PUT` so'rovi uchun yozuvlarga aylantiradi. */
export function collectChanges(
  entries: RolePermissionEntry[],
  changes: ReadonlyMap<MatrixCellKey, boolean>,
): RolePermissionEntry[] {
  return entries.flatMap((entry) => {
    const granted = changes.get(matrixCellKey(entry.role, entry.permission));
    return granted === undefined || granted === entry.granted ? [] : [{ ...entry, granted }];
  });
}
