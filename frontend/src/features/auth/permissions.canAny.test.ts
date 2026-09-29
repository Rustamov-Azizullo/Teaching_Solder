import { canAny } from './permissions';
import type { AuthUser } from './types';

const user = (permissions: AuthUser['permissions']): AuthUser => ({
  id: 1, username: 'u', fullName: 'U', role: 'USER', roleLabel: 'User', locationId: null, locationName: null,
  locationLevel: null, militaryUnitId: null, active: true, permissions,
});

describe('canAny', () => {
  it('passes when at least one capability is held', () => {
    expect(canAny(user(['DICTIONARY_WRITE']), ['dictionaryWrite', 'unitDirections'])).toBe(true);
    expect(canAny(user(['UNIT_DIRECTIONS']), ['dictionaryWrite', 'unitDirections'])).toBe(true);
  });

  it('fails when none is held', () => {
    expect(canAny(user(['REPORTS']), ['dictionaryWrite', 'unitDirections'])).toBe(false);
    expect(canAny(null, ['dictionaryWrite'])).toBe(false);
  });
});
