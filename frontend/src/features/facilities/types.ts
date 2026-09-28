import type { NamedRef } from '@/types/api';

export type FacilityKind = 'CLASSROOM' | 'WORKSHOP' | 'TRAINING_AREA' | 'OTHER';
export type FacilityCondition = 'GOOD' | 'SATISFACTORY' | 'POOR' | 'UNFIT';

export type Facility = {
  id: number;
  militaryUnitId: number;
  militaryUnitName: string;
  name: string;
  kind: FacilityKind;
  capacity: number | null;
  condition: FacilityCondition;
  equipment: string | null;
  shortages: string | null;
  surveyDate: string | null;
  suitableFor: NamedRef[];
};

export type FacilityRequest = {
  militaryUnitId: number;
  name: string;
  kind: FacilityKind;
  capacity?: number;
  condition: FacilityCondition;
  equipment?: string;
  shortages?: string;
  surveyDate?: string;
  suitableForIds: number[];
};
