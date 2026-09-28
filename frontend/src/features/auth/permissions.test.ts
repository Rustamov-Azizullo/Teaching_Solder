import { can, isPermissionManager } from './permissions';
import type { AuthUser, PermissionKey, Role } from './types';

const makeUser = (role: Role, permissions: PermissionKey[] = []): AuthUser => ({
  id: 1,
  username: 'user',
  fullName: 'Test User',
  role,
  roleLabel: role,
  locationId: null,
  locationName: null,
  locationLevel: null,
  militaryUnitId: null,
  active: true,
  permissions,
});

describe('can', () => {
  it.each([
    ['MEGA_SUPER_ADMIN', [], 'admin'],
    ['MEGA_SUPER_ADMIN', [], 'soldierWrite'],
  ] as const)('%s with no explicit permissions is always allowed %s', (role, permissions, capability) => {
    expect(can(makeUser(role, [...permissions]), capability)).toBe(true);
  });

  it.each([
    ['SUPER_ADMIN', [], 'systemConfig', false],
    ['SUPER_ADMIN', ['SYSTEM_CONFIG'], 'systemConfig', true],
    ['ADMIN', ['ASSIGNMENT_DECIDE'], 'assignmentDecide', true],
    ['ADMIN', ['ASSIGNMENT_READ'], 'assignmentDecide', false],
    ['USER', ['EMPLOYMENT'], 'employment', true],
    ['USER', [], 'soldierRead', false],
    ['USER', ['GROUP_LEADER_ASSIGN'], 'leaderAssign', true],
    ['USER', ['UNIT_DIRECTIONS'], 'unitDirections', true],
    ['ADMIN', ['REPORTS'], 'reports', true],
    ['ADMIN', ['ADMIN'], 'systemConfig', false],
    ['ADMIN', ['SYSTEM_CONFIG'], 'systemConfig', true],
  ] as const)('%s with %j / %s -> %s', (role, permissions, capability, expected) => {
    expect(can(makeUser(role, [...permissions]), capability)).toBe(expected);
  });

  it('denies every capability when there is no user', () => {
    expect(can(undefined, 'admin')).toBe(false);
    expect(can(null, 'soldierRead')).toBe(false);
  });
});

describe('isPermissionManager', () => {
  it.each([
    ['MEGA_SUPER_ADMIN', true],
    ['SUPER_ADMIN', true],
    ['ADMIN', false],
    ['USER', false],
  ] as const)('%s -> %s', (role, expected) => {
    expect(isPermissionManager(makeUser(role))).toBe(expected);
  });

  it('ignores dynamically granted permissions (static role gate)', () => {
    expect(isPermissionManager(makeUser('ADMIN', ['ADMIN', 'SYSTEM_CONFIG']))).toBe(false);
  });

  it('returns false when there is no user', () => {
    expect(isPermissionManager(null)).toBe(false);
  });
});
