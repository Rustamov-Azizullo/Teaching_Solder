export type DashboardFilters = {
  districtId?: number;
  unitId?: number;
};

export type UnitSoldiers = { id: number; name: string; soldiers: number };
export type DistrictSoldiers = { id: number; name: string; soldiers: number; units: UnitSoldiers[] };
export type SubdivisionSoldiers = { id: number | null; name: string; soldiers: number };
export type ProfessionRow = {
  profession: string; soldiers: number; districts: DistrictSoldiers[]; subdivisions: SubdivisionSoldiers[];
};
export type RegionRow = { name: string; soldiers: number };
export type ProgramRow = { profession: string; institution: string; soldiers: number };
export type DistrictRow = {
  id: number | null; name: string; soldiers: number; vocational: number; otm: number;
  professions: number; institutions: number; programs: ProgramRow[];
};
export type Overview = {
  totalSoldiers: number;
  vocationalStudying: number;
  otmPreparing: number;
  unassigned: number;
  certified: number;
  higherCompleted: number;
  higherIncomplete: number;
  professions: ProfessionRow[];
  districts: DistrictRow[];
  regions: RegionRow[];
};
