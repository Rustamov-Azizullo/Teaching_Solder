import type { CourseStatusValue } from './statuses';

export type EmploymentRow = {
  soldierId: number;
  fullName: string;
  birthDate: string;
  pinfl: string;
  district: string;
  mahalla: string | null;
  phone: string;
  profession: string | null;
  status: CourseStatusValue;
  certificateNo: string | null;
};

export type RegionGroup = { regionId: number; regionName: string; rows: EmploymentRow[] };
export type ExportFormat = 'XLSX' | 'PDF';
export type ExportRequest = { agency: string; regionId?: number; format: ExportFormat };
export type HistoryRow = { id: number; regionName: string; agency: string; soldierCount: number; format: string; generatedAt: string; generatedBy: string };
