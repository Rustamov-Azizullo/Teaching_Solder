import type { MatrixCellKey, RolePermissionEntry } from '../types';
import { buildMatrixRows, canEditRole, collectChanges, matrixCellKey } from './rolePermissionMatrix';

const entries: RolePermissionEntry[] = [
  { role: 'ADMIN', permission: 'ADMIN', granted: true },
  { role: 'ADMIN', permission: 'SOLDIER_READ', granted: true },
  { role: 'USER', permission: 'ADMIN', granted: false },
  { role: 'USER', permission: 'SOLDIER_READ', granted: true },
];

describe('buildMatrixRows', () => {
  it('groups entries into one row per permission, keeping catalog order', () => {
    expect(buildMatrixRows(entries)).toEqual([
      { permission: 'ADMIN', granted: { SUPER_ADMIN: false, ADMIN: true, USER: false } },
      { permission: 'SOLDIER_READ', granted: { SUPER_ADMIN: false, ADMIN: true, USER: true } },
    ]);
  });
});

describe('collectChanges', () => {
  it('returns only cells whose value differs from the server state', () => {
    const changes = new Map<MatrixCellKey, boolean>([
      [matrixCellKey('USER', 'ADMIN'), true],
      [matrixCellKey('ADMIN', 'SOLDIER_READ'), true],
    ]);

    expect(collectChanges(entries, changes)).toEqual([{ role: 'USER', permission: 'ADMIN', granted: true }]);
  });

  it('returns nothing when there are no changes', () => {
    expect(collectChanges(entries, new Map())).toEqual([]);
  });
});

describe('canEditRole', () => {
  it('lets only Mega SuperAdmin edit SuperAdmin permissions', () => {
    expect(canEditRole('MEGA_SUPER_ADMIN', 'SUPER_ADMIN')).toBe(true);
    expect(canEditRole('SUPER_ADMIN', 'SUPER_ADMIN')).toBe(false);
    expect(canEditRole('SUPER_ADMIN', 'ADMIN')).toBe(true);
  });
});
