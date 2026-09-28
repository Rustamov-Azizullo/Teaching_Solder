import type { Dayjs } from 'dayjs';
import type { NamedRef } from '@/types/api';

export type GeneralEducation = 'SCHOOL' | 'LYCEUM';
export type ProfessionalEducation = 'VOCATIONAL_SCHOOL' | 'TECHNICAL_SCHOOL' | 'COLLEGE';
export type HigherEducation = 'BACHELOR' | 'MASTER' | 'INCOMPLETE_HIGHER';
export type CertificateKind = 'LANGUAGE' | 'PROFESSION' | 'SUBJECT';
export type AwardPlace = 'FIRST' | 'SECOND' | 'THIRD' | 'GRAND_PRIX';
export type FieldSource = 'INTEGRATION' | 'MANUAL';

export type SoldierSummary = {
  id: number;
  pinfl: string;
  fullName: string;
  birthDate: string;
  militaryUnitName: string;
  regionName: string;
  districtName: string;
  subdivisionPath: string | null;
};

export type Certificate = {
  id: number;
  kind: CertificateKind;
  dictionaryItemId: number | null;
  dictionaryItemName: string | null;
  title: string | null;
  level: string | null;
};

export type Award = { id: number; kindId: number; kindName: string; place: AwardPlace };

export type FieldSourceInfo = { source: FieldSource; filledBy: string; filledAt: string };

export type Soldier = {
  id: number;
  pinfl: string;
  fullName: string;
  birthDate: string;
  passport: string;
  phone: string;
  phoneKinshipId: number | null;
  phoneKinshipName: string | null;
  regionId: number;
  regionName: string;
  districtId: number;
  districtName: string;
  mahalla: string | null;
  street: string | null;
  house: string | null;
  apartment: string | null;
  militaryUnitId: number;
  militaryUnitName: string;
  conscriptionDate: string | null;
  serviceEndDate: string | null;
  generalEducation: GeneralEducation;
  professionalEducation: ProfessionalEducation | null;
  higherEducation: HigherEducation | null;
  priorOccupation: string | null;
  noPriorOccupation: boolean;
  trainable: boolean | null;
  subdivisionId: number | null;
  subdivisionPath: string | null;
  cycleYear: number;
  certificates: Certificate[];
  awards: Award[];
  fieldSources: Record<string, FieldSourceInfo>;
};

export type SourceLookup = {
  found: boolean;
  existingSoldierId: number | null;
  fullName: string | null;
  birthDate: string | null;
  passport: string | null;
  regionId: number | null;
  districtId: number | null;
  mahalla: string | null;
  street: string | null;
  house: string | null;
  generalEducation: GeneralEducation | null;
};

export type SoldierRequest = {
  pinfl: string;
  fullName: string;
  birthDate: string;
  passport: string;
  phone: string;
  phoneKinshipId: number;
  regionId: number;
  districtId: number;
  mahalla: string;
  street: string;
  house: string;
  apartment?: string;
  militaryUnitId: number;
  subdivisionId?: number;
  conscriptionDate?: string;
  serviceEndDate?: string;
  generalEducation: GeneralEducation;
  professionalEducation?: ProfessionalEducation;
  higherEducation?: HigherEducation;
  priorOccupation?: string;
  noPriorOccupation: boolean;
  trainable?: boolean;
  certificates: { kind: CertificateKind; dictionaryItemId?: number; title?: string; level?: string }[];
  awards: { kindId: number; place: AwardPlace }[];
  fieldSources: Record<string, FieldSource>;
};

/** Ant Design formasidagi qiymatlar (sanalar Dayjs ko'rinishida). */
export type SoldierFormValues = {
  pinfl: string;
  fullName: string;
  birthDate: Dayjs;
  passport: string;
  phone: string;
  phoneKinshipId: number;
  regionId: number;
  districtId: number;
  mahalla: string;
  street: string;
  house: string;
  apartment?: string;
  militaryUnitId: number;
  subdivisionId?: number;
  conscriptionDate?: Dayjs | null;
  serviceEndDate?: Dayjs | null;
  generalEducation: GeneralEducation;
  professionalEducation?: ProfessionalEducation;
  higherEducation?: HigherEducation;
  priorOccupation?: string;
  noPriorOccupation: boolean;
  trainable?: boolean;
  languageCertificates?: { dictionaryItemId: number; level?: string }[];
  professionCertificates?: { title: string }[];
  subjectCertificates?: { dictionaryItemId: number }[];
  awards?: { kindId: number; place: AwardPlace }[];
};

export type SoldierSearchParams = { query?: string; unitId?: number; subdivisionId?: number; page: number; size: number };

export type TransferRecord = {
  id: number;
  fromUnit: string;
  toUnit: string;
  fromSubdivision: string | null;
  toSubdivision: string | null;
  reason: string;
  transferredAt: string;
  transferredBy: string;
};

export type TransferRequest = { militaryUnitId: number; subdivisionId?: number; reason: string };

export type FieldDiff = { field: string; label: string; current: string | null; incoming: string | null };

export type ImportResult = { imported: number; errors: { row: number; message: string }[] };

export type Ref = NamedRef;
