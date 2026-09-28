import type { Location } from '../types';
import { buildLocationOptions, locationLevelForRole } from './locationOptions';

const location = (id: number, level: Location['level'], name: string, parentId: number | null): Location => ({
  id, level, name, parentId, code: null, militaryDistrictId: null, militaryUnitId: null,
});

const locations: Location[] = [
  location(1, 'REPUBLIC', "O'zbekiston", null),
  location(2, 'DISTRICT', 'Toshkent HO', 1),
  location(3, 'DISTRICT', 'Janubi-g\'arbiy HO', 1),
  location(4, 'UNIT', '12345-qism', 2),
  location(5, 'UNIT', '54321-qism', 3),
  location(6, 'UNIT', '11111-qism', 2),
];

describe('locationLevelForRole', () => {
  it.each([
    ['MEGA_SUPER_ADMIN', null],
    ['SUPER_ADMIN', null],
    ['ADMIN', 'DISTRICT'],
    ['USER', 'UNIT'],
    [undefined, null],
  ] as const)('%s -> %s', (role, expected) => {
    expect(locationLevelForRole(role)).toBe(expected);
  });
});

describe('buildLocationOptions', () => {
  it('lists only districts, flat and sorted, for the district level', () => {
    expect(buildLocationOptions(locations, 'DISTRICT')).toEqual([
      { value: 3, label: "Janubi-g'arbiy HO" },
      { value: 2, label: 'Toshkent HO' },
    ]);
  });

  it('groups units under their parent district for the unit level', () => {
    expect(buildLocationOptions(locations, 'UNIT')).toEqual([
      { label: "Janubi-g'arbiy HO", options: [{ value: 5, label: '54321-qism' }] },
      { label: 'Toshkent HO', options: [{ value: 6, label: '11111-qism' }, { value: 4, label: '12345-qism' }] },
    ]);
  });
});
