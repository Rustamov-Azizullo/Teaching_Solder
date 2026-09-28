import type { PermissionKey } from '@/features/auth';
import { PERMISSION_GROUPS, sortByGroup } from './permissionGroups';

describe('sortByGroup', () => {
  it('orders rows by group while keeping catalog order inside a group', () => {
    const rows: { permission: PermissionKey }[] = [
      { permission: 'REPORTS' },
      { permission: 'SOLDIER_WRITE' },
      { permission: 'ADMIN' },
      { permission: 'SOLDIER_READ' },
    ];

    expect(sortByGroup(rows).map((row) => row.permission)).toEqual(['ADMIN', 'SOLDIER_WRITE', 'SOLDIER_READ', 'REPORTS']);
  });

  it('assigns every permission to a group', () => {
    expect(Object.values(PERMISSION_GROUPS).every(Boolean)).toBe(true);
  });
});
