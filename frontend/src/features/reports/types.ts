export type ReportType = 'COURSE_COMPLETION' | 'OTM_ADMISSIONS' | 'YEARLY_SUMMARY';
export type ReportFormat = 'XLSX' | 'PDF';
export type ReportGroupType = 'VOCATIONAL' | 'OTM_PREP';

export type ReportParams = { type: ReportType; format: ReportFormat; groupType: ReportGroupType; from: string; to: string };
