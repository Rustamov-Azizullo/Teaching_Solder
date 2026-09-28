export type DailyPoint = { date: string; percent: number };
export type WeeklyPoint = { weekStart: string; averagePercent: number; academicHours: number };
export type BreakdownRow = { id: number; label: string; percent: number; present: number; total: number };
export type BreakdownLevel = 'DISTRICT' | 'UNIT' | 'GROUP';
export type Breakdown = { level: BreakdownLevel; rows: BreakdownRow[] };
export type ReasonSlice = { reason: string; label: string; count: number };

export type AttendanceSummary = {
  totalSoldiers: number;
  totalGroups: number;
  todayPercent: number;
  groupsWithoutAttendanceToday: number;
};

export type AttendanceBlock = {
  summary: AttendanceSummary;
  daily: DailyPoint[];
  weekly: WeeklyPoint[];
  breakdown: Breakdown;
  absenceReasons: ReasonSlice[];
};

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

export type AttendanceKind = 'vocational' | 'otm';

export type DashboardFilters = {
  from: string;
  to: string;
  districtId?: number;
  unitId?: number;
};
