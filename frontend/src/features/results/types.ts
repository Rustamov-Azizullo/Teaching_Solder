export type CourseStatus = 'STUDIED' | 'EXAM_PASSED' | 'CERTIFIED' | 'DROPPED';

export type ResultRow = {
  soldierId: number;
  fullName: string;
  pinfl: string;
  status: CourseStatus | null;
  examGrade: number | null;
  dropReason: string | null;
  certificateNo: string | null;
  certificateDate: string | null;
  certificateIssuer: string | null;
};

export type ResultsSheet = {
  groupId: number;
  groupName: string;
  type: 'VOCATIONAL' | 'OTM_PREP';
  approved: boolean;
  approvedBy: string | null;
  approvedAt: string | null;
  rows: ResultRow[];
};

export type ResultInput = {
  soldierId: number;
  status: CourseStatus;
  examGrade?: number | null;
  dropReason?: string;
  certificateNo?: string;
  certificateDate?: string;
  certificateIssuer?: string;
};
