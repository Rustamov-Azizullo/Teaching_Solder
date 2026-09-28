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

export type Region = NamedRef;
export type TerritorialDistrict = NamedRef;
export type MilitaryDistrict = NamedRef;
