export type ReportType = 'WEEKLY_UNIT_SUMMARY' | 'COURSE_COMPLETION' | 'OTM_ADMISSIONS' | 'YEARLY_SUMMARY';
export type ReportFormat = 'XLSX' | 'PDF';

export type ReportParams = { type: ReportType; format: ReportFormat; year?: number };
