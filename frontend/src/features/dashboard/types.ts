export type CountItem = { label: string; count: number };
export type SubjectNeed = { subject: string; specialtyCount: number; mandatoryCount: number };
export type CompletionRow = { unitId: number; unitName: string; soldiers: number; finalized: number; percent: number };

export type SurveyBlock = {
  totalSoldiers: number;
  finalizedQuestionnaires: number;
  completion: CompletionRow[];
  interests: CountItem[];
  futurePlans: CountItem[];
  subjectNeeds: SubjectNeed[];
  education: { educationLevels: CountItem[]; certificatesAndAwards: CountItem[] };
};

export type DashboardFilters = {
  districtId?: number;
  unitId?: number;
};

export type UnitContract = { unitId: number; unitName: string; status: string; contractNo: string | null; contractDate: string | null };
export type InstitutionContracts = { id: number; name: string; type: string; units: UnitContract[] };
export type RegionInstitutions = {
  region: string;
  soldiers: number;
  institutionCount: number;
  contractCount: number;
  institutions: InstitutionContracts[];
};
export type UnitSoldiers = { id: number; name: string; soldiers: number };
export type DistrictSoldiers = { id: number; name: string; soldiers: number; units: UnitSoldiers[] };
export type GeographyBlock = { totalSoldiers: number; districts: DistrictSoldiers[]; regions: RegionInstitutions[] };
