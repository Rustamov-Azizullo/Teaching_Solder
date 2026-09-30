export type ReportType = 'WEEKLY_UNIT_SUMMARY' | 'COURSE_COMPLETION' | 'OTM_ADMISSIONS' | 'YEARLY_SUMMARY';
export type ReportFormat = 'XLSX' | 'PDF';
/** Haftalik hisobot ko'rinishi: kesim (okruglar / harbiy qismlar) yoki umumiy. */
export type ReportView = 'BREAKDOWN' | 'TOTAL';

export type ReportParams = {
  type: ReportType; format: ReportFormat; year?: number; view?: ReportView;
  districtId?: number; unitId?: number; subdivisionId?: number;
};

/** Haftalik hisobot tanlovi: kesim, umumiy yoki aniq bitta okrug / harbiy qism / bo'linma (rolga qarab). */
export type WeeklyMode = ReportView | 'DISTRICT' | 'UNIT' | 'SUBDIVISION';
export type WeeklySelection = { mode: WeeklyMode; targetId?: number };
