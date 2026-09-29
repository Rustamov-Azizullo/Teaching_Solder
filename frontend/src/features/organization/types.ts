import type { NamedRef } from '@/types/api';

export type MilitaryUnit = {
  id: number;
  name: string;
  militaryDistrictId: number;
  militaryDistrictName: string;
};

export type SubdivisionNode = {
  id: number;
  name: string;
  parentId: number | null;
  soldierCount: number;
  children: SubdivisionNode[];
};

/** `GET /api/locations/tree` elementi: vazirlik → okrug → harbiy qism (tekis ro'yxat, `parentId` bilan). */
export type LocationNode = {
  id: number;
  parentId: number | null;
  level: 'REPUBLIC' | 'DISTRICT' | 'UNIT';
  name: string;
  militaryDistrictId: number | null;
  militaryUnitId: number | null;
};

export type Region = NamedRef;
export type TerritorialDistrict = NamedRef;
export type MilitaryDistrict = NamedRef;
