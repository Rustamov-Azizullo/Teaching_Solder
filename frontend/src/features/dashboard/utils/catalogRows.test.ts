import { describe, expect, it } from 'vitest';
import type { CatalogUnit } from '../types';
import { toDirectionRows, toEntryRows } from './catalogRows';

const units: CatalogUnit[] = [
  { id: 1, name: 'Qism 1', directions: ['IT', 'Payvand'], professions: [{ name: 'Dasturchi', groups: 1, soldiers: 10 }], subjects: [] },
  { id: 2, name: 'Qism 2', directions: ['IT'], professions: [{ name: 'Dasturchi', groups: 2, soldiers: 5 }, { name: 'Oshpaz', groups: 1, soldiers: 20 }], subjects: [] },
];

describe('catalogRows', () => {
  it('groups directions by name with the units that have them', () => {
    expect(toDirectionRows(units)).toEqual([
      { name: 'IT', groups: 0, soldiers: 0 },
      { name: 'Payvand', groups: 0, soldiers: 0 },
    ]);
  });

  it('sums groups and soldiers per name and sorts by soldiers', () => {
    expect(toEntryRows(units, (unit) => unit.professions)).toEqual([
      { name: 'Oshpaz', groups: 1, soldiers: 20 },
      { name: 'Dasturchi', groups: 3, soldiers: 15 },
    ]);
  });
});
