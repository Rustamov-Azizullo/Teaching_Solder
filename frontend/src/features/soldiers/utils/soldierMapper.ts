import { API_DATE_FORMAT, dayjs } from '@/lib/dayjs';
import type { Soldier, SoldierFormValues, SoldierRequest, FieldSource, SourceLookup } from '../types';

/** Manba belgisi kuzatiladigan maydonlar (integratsiya orqali kelishi mumkin bo'lganlar). */
export const SOURCE_TRACKED_FIELDS = [
  'fullName', 'birthDate', 'passport', 'regionId', 'districtId', 'mahalla', 'street', 'house', 'generalEducation',
] as const;

export type TrackedField = (typeof SOURCE_TRACKED_FIELDS)[number];

const toApiDate = (value?: SoldierFormValues['birthDate'] | null): string | undefined =>
  value ? value.format(API_DATE_FORMAT) : undefined;

export function toSoldierRequest(
  values: SoldierFormValues,
  integrationFields: ReadonlySet<string>,
): SoldierRequest {
  const fieldSources = Object.fromEntries(
    SOURCE_TRACKED_FIELDS.map((field): [string, FieldSource] => [
      field,
      integrationFields.has(field) ? 'INTEGRATION' : 'MANUAL',
    ]),
  );
  return {
    pinfl: values.pinfl,
    fullName: values.fullName.trim(),
    birthDate: toApiDate(values.birthDate) as string,
    passport: values.passport.toUpperCase(),
    phone: values.phone,
    phoneKinshipId: values.phoneKinshipId,
    regionId: values.regionId,
    districtId: values.districtId,
    mahalla: values.mahalla,
    street: values.street,
    house: values.house,
    apartment: values.apartment,
    militaryUnitId: values.militaryUnitId,
    subdivisionId: values.subdivisionId,
    conscriptionDate: toApiDate(values.conscriptionDate),
    serviceEndDate: toApiDate(values.serviceEndDate),
    generalEducation: values.generalEducation,
    professionalEducation: values.professionalEducation,
    higherEducation: values.higherEducation,
    priorOccupation: values.noPriorOccupation ? undefined : values.priorOccupation,
    noPriorOccupation: values.noPriorOccupation,
    trainable: values.trainable,
    certificates: [
      ...(values.languageCertificates ?? []).map((c) => ({ kind: 'LANGUAGE' as const, ...c })),
      ...(values.professionCertificates ?? []).map((c) => ({ kind: 'PROFESSION' as const, title: c.title })),
      ...(values.subjectCertificates ?? []).map((c) => ({ kind: 'SUBJECT' as const, ...c })),
    ],
    awards: values.awards ?? [],
    fieldSources,
  };
}

export function toFormValues(soldier: Soldier): SoldierFormValues {
  const certificatesOf = (kind: 'LANGUAGE' | 'PROFESSION' | 'SUBJECT') =>
    soldier.certificates.filter((certificate) => certificate.kind === kind);
  return {
    pinfl: soldier.pinfl,
    fullName: soldier.fullName,
    birthDate: dayjs(soldier.birthDate),
    passport: soldier.passport,
    phone: soldier.phone,
    phoneKinshipId: soldier.phoneKinshipId as number,
    regionId: soldier.regionId,
    districtId: soldier.districtId,
    mahalla: soldier.mahalla ?? '',
    street: soldier.street ?? '',
    house: soldier.house ?? '',
    apartment: soldier.apartment ?? undefined,
    militaryUnitId: soldier.militaryUnitId,
    subdivisionId: soldier.subdivisionId ?? undefined,
    conscriptionDate: soldier.conscriptionDate ? dayjs(soldier.conscriptionDate) : null,
    serviceEndDate: soldier.serviceEndDate ? dayjs(soldier.serviceEndDate) : null,
    generalEducation: soldier.generalEducation,
    professionalEducation: soldier.professionalEducation ?? undefined,
    higherEducation: soldier.higherEducation ?? undefined,
    priorOccupation: soldier.priorOccupation ?? undefined,
    noPriorOccupation: soldier.noPriorOccupation,
    trainable: soldier.trainable ?? undefined,
    languageCertificates: certificatesOf('LANGUAGE').map((c) => ({
      dictionaryItemId: c.dictionaryItemId as number,
      level: c.level ?? undefined,
    })),
    professionCertificates: certificatesOf('PROFESSION').map((c) => ({ title: c.title ?? '' })),
    subjectCertificates: certificatesOf('SUBJECT').map((c) => ({ dictionaryItemId: c.dictionaryItemId as number })),
    awards: soldier.awards.map((a) => ({ kindId: a.kindId, place: a.place })),
  };
}

/** Manba tizim javobini forma qiymatlariga aylantiradi va qaysi maydonlar to'ldirilganini qaytaradi. */
export function toPrefill(lookup: SourceLookup): {
  values: Partial<SoldierFormValues>;
  fields: TrackedField[];
} {
  const values: Partial<SoldierFormValues> = {};
  const fields: TrackedField[] = [];
  const assign = <K extends TrackedField>(field: K, value: SoldierFormValues[K] | null | undefined) => {
    if (value === null || value === undefined) return;
    values[field] = value;
    fields.push(field);
  };
  assign('fullName', lookup.fullName);
  assign('birthDate', lookup.birthDate ? dayjs(lookup.birthDate) : null);
  assign('passport', lookup.passport);
  assign('regionId', lookup.regionId);
  assign('districtId', lookup.districtId);
  assign('mahalla', lookup.mahalla);
  assign('street', lookup.street);
  assign('house', lookup.house);
  assign('generalEducation', lookup.generalEducation);
  return { values, fields };
}
