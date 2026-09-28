import type { MatrixCellKey, RolePermissionEntry } from '../types';
import { buildMatrixRows, collectChanges, matrixCellKey } from './rolePermissionMatrix';

const entries: RolePermissionEntry[] = [
  { role: 'ADMIN', permission: 'ADMIN', granted: true },
  { role: 'ADMIN', permission: 'SOLDIER_READ', granted: true },
  { role: 'USER', permission: 'ADMIN', granted: false },
  { role: 'USER', permission: 'SOLDIER_READ', granted: true },
];

describe('buildMatrixRows', () => {
  it('groups entries into one row per permission, keeping catalog order', () => {
    expect(buildMatrixRows(entries)).toEqual([
      { permission: 'ADMIN', granted: { ADMIN: true, USER: false } },
      { permission: 'SOLDIER_READ', granted: { ADMIN: true, USER: true } },
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
