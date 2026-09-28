import type { RoleOption } from '../types';
import { toCreateUserRequest } from './locationUser';

const roles: RoleOption[] = [
  { code: 'SUPER_ADMIN', label: 'SuperAdmin', scopeLevel: 'REPUBLIC' },
  { code: 'ADMIN', label: 'Admin', scopeLevel: 'DISTRICT' },
  { code: 'USER', label: 'User', scopeLevel: 'UNIT' },
];
const values = { username: 'yangi', password: 'Parol123!', fullName: 'Yangi Xodim' };

describe('toCreateUserRequest', () => {
  it('derives the role from the location level and attaches the location', () => {
    expect(toCreateUserRequest(values, 'DISTRICT', 7, roles)).toEqual({ ...values, role: 'ADMIN', locationId: 7 });
    expect(toCreateUserRequest(values, 'UNIT', 9, roles)?.role).toBe('USER');
  });

  it('does not attach republic-level roles to a location', () => {
    expect(toCreateUserRequest(values, 'REPUBLIC', 1, roles)?.locationId).toBeUndefined();
  });

  it('prefers an explicitly chosen role and returns null when none fits', () => {
    expect(toCreateUserRequest({ ...values, role: 'SUPER_ADMIN' }, 'REPUBLIC', 1, roles)?.role).toBe('SUPER_ADMIN');
    expect(toCreateUserRequest(values, 'UNIT', 9, [])).toBeNull();
  });
});
