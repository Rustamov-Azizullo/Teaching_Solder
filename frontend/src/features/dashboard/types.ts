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
