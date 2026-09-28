import type { PermissionKey } from '@/features/auth';
import type { ConfigurableRole, MatrixCellKey, RolePermissionEntry, RolePermissionRow } from '../types';

export const CONFIGURABLE_ROLES: readonly ConfigurableRole[] = ['ADMIN', 'USER'];

export function matrixCellKey(role: ConfigurableRole, permission: PermissionKey): MatrixCellKey {
  return `${role}:${permission}`;
}

/** Tekis ro'yxatni ruxsat bo'yicha qatorlarga yig'adi (backend katalogi tartibi saqlanadi). */
export function buildMatrixRows(entries: RolePermissionEntry[]): RolePermissionRow[] {
  const rows = new Map<PermissionKey, RolePermissionRow>();
  for (const entry of entries) {
    const row = rows.get(entry.permission) ?? { permission: entry.permission, granted: { ADMIN: false, USER: false } };
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
