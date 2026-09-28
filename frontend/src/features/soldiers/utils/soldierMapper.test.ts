import { dayjs } from '@/lib/dayjs';
import type { Soldier, SoldierFormValues, SourceLookup } from '../types';
import { SOURCE_TRACKED_FIELDS, toFormValues, toPrefill, toSoldierRequest } from './soldierMapper';

const baseValues = (overrides: Partial<SoldierFormValues> = {}): SoldierFormValues => ({
  pinfl: '12345678901234',
  fullName: '  Ali Valiyev  ',
  birthDate: dayjs('2005-04-09'),
  passport: 'aa1234567',
  phone: '+998901234567',
  phoneKinshipId: 1,
  regionId: 2,
  districtId: 3,
  mahalla: 'M',
  street: 'S',
  house: '5',
  militaryUnitId: 7,
  generalEducation: 'SCHOOL',
  noPriorOccupation: false,
  priorOccupation: 'Driver',
  ...overrides,
});

describe('toSoldierRequest', () => {
  it('marks tracked fields INTEGRATION only when present in the integration set', () => {
    const request = toSoldierRequest(baseValues(), new Set(['fullName', 'passport']));

    expect(request.fieldSources.fullName).toBe('INTEGRATION');
    expect(request.fieldSources.passport).toBe('INTEGRATION');
    expect(request.fieldSources.birthDate).toBe('MANUAL');
    expect(Object.keys(request.fieldSources).sort()).toEqual([...SOURCE_TRACKED_FIELDS].sort());
  });

  it('marks all fields MANUAL for an empty integration set', () => {
    const request = toSoldierRequest(baseValues(), new Set());
    expect(Object.values(request.fieldSources).every((source) => source === 'MANUAL')).toBe(true);
  });

  it('trims the name, uppercases the passport and formats dates for the API', () => {
    const request = toSoldierRequest(
      baseValues({ conscriptionDate: dayjs('2024-01-15'), serviceEndDate: null }),
      new Set(),
    );

    expect(request.fullName).toBe('Ali Valiyev');
    expect(request.passport).toBe('AA1234567');
    expect(request.birthDate).toBe('2005-04-09');
    expect(request.conscriptionDate).toBe('2024-01-15');
    expect(request.serviceEndDate).toBeUndefined();
  });

  it('keeps priorOccupation when the soldier had one', () => {
    expect(toSoldierRequest(baseValues(), new Set()).priorOccupation).toBe('Driver');
  });

  it('clears priorOccupation when noPriorOccupation is set', () => {
    const request = toSoldierRequest(baseValues({ noPriorOccupation: true }), new Set());
    expect(request.priorOccupation).toBeUndefined();
    expect(request.noPriorOccupation).toBe(true);
  });

  it('flattens the three certificate lists into one tagged list', () => {
    const request = toSoldierRequest(
      baseValues({
        languageCertificates: [{ dictionaryItemId: 10, level: 'B2' }],
        professionCertificates: [{ title: 'Welder' }],
        subjectCertificates: [{ dictionaryItemId: 20 }],
      }),
      new Set(),
    );

    expect(request.certificates).toEqual([
      { kind: 'LANGUAGE', dictionaryItemId: 10, level: 'B2' },
      { kind: 'PROFESSION', title: 'Welder' },
      { kind: 'SUBJECT', dictionaryItemId: 20 },
    ]);
  });

  it('defaults certificates and awards to empty arrays', () => {
    const request = toSoldierRequest(baseValues(), new Set());
    expect(request.certificates).toEqual([]);
    expect(request.awards).toEqual([]);
  });
});

describe('toPrefill', () => {
  const lookup = (overrides: Partial<SourceLookup> = {}): SourceLookup => ({
    found: true,
    existingSoldierId: null,
    fullName: 'Ali Valiyev',
    birthDate: '2005-04-09',
    passport: 'AA1234567',
    regionId: 2,
    districtId: 3,
    mahalla: 'M',
    street: null,
    house: null,
    generalEducation: 'LYCEUM',
    ...overrides,
  });

  it('maps present lookup fields to form values and lists them', () => {
    const { values, fields } = toPrefill(lookup());

    expect(values.fullName).toBe('Ali Valiyev');
    expect(values.birthDate?.format('YYYY-MM-DD')).toBe('2005-04-09');
    expect(values.generalEducation).toBe('LYCEUM');
    expect(fields).toEqual([
      'fullName', 'birthDate', 'passport', 'regionId', 'districtId', 'mahalla', 'generalEducation',
    ]);
  });

  it('skips null fields', () => {
    const { values, fields } = toPrefill(lookup());
    expect(fields).not.toContain('street');
    expect(fields).not.toContain('house');
    expect(values).not.toHaveProperty('street');
  });

  it('returns nothing when the lookup is empty', () => {
    const empty = lookup({
      fullName: null, birthDate: null, passport: null, regionId: null, districtId: null,
      mahalla: null, generalEducation: null,
    });
    expect(toPrefill(empty)).toEqual({ values: {}, fields: [] });
  });
});

describe('toFormValues', () => {
  const soldier: Soldier = {
    id: 1,
    subdivisionId: null,
    subdivisionPath: null,
    pinfl: '12345678901234',
    fullName: 'Ali Valiyev',
    birthDate: '2005-04-09',
    passport: 'AA1234567',
    phone: '+998901234567',
    phoneKinshipId: 1,
    phoneKinshipName: 'Ota',
    regionId: 2,
    regionName: 'R',
    districtId: 3,
    districtName: 'D',
    mahalla: 'M',
    street: 'S',
    house: '5',
    apartment: null,
    militaryUnitId: 7,
    militaryUnitName: 'U',
    conscriptionDate: '2024-01-15',
    serviceEndDate: null,
    generalEducation: 'SCHOOL',
    professionalEducation: null,
    higherEducation: null,
    priorOccupation: 'Driver',
    noPriorOccupation: false,
    trainable: null,
    cycleYear: 2024,
    certificates: [
      { id: 1, kind: 'LANGUAGE', dictionaryItemId: 10, dictionaryItemName: 'EN', title: null, level: 'B2' },
      { id: 2, kind: 'PROFESSION', dictionaryItemId: null, dictionaryItemName: null, title: 'Welder', level: null },
      { id: 3, kind: 'SUBJECT', dictionaryItemId: 20, dictionaryItemName: 'Math', title: null, level: null },
    ],
    awards: [{ id: 1, kindId: 4, kindName: 'Olympiad', place: 'FIRST' }],
    fieldSources: {},
  };

  it('splits certificates by kind and converts dates', () => {
    const form = toFormValues(soldier);

    expect(form.languageCertificates).toEqual([{ dictionaryItemId: 10, level: 'B2' }]);
    expect(form.professionCertificates).toEqual([{ title: 'Welder' }]);
    expect(form.subjectCertificates).toEqual([{ dictionaryItemId: 20 }]);
    expect(form.awards).toEqual([{ kindId: 4, place: 'FIRST' }]);
    expect(form.conscriptionDate?.format('YYYY-MM-DD')).toBe('2024-01-15');
    expect(form.serviceEndDate).toBeNull();
    expect(form.apartment).toBeUndefined();
  });

  it('round-trips into a request equivalent to the original soldier data', () => {
    const request = toSoldierRequest(toFormValues(soldier), new Set());

    expect(request.pinfl).toBe(soldier.pinfl);
    expect(request.birthDate).toBe('2005-04-09');
    expect(request.conscriptionDate).toBe('2024-01-15');
    expect(request.priorOccupation).toBe('Driver');
    expect(request.certificates).toEqual([
      { kind: 'LANGUAGE', dictionaryItemId: 10, level: 'B2' },
      { kind: 'PROFESSION', title: 'Welder' },
      { kind: 'SUBJECT', dictionaryItemId: 20 },
    ]);
    expect(request.awards).toEqual([{ kindId: 4, place: 'FIRST' }]);
  });
});
