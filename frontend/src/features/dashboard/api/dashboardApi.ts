import { apiClient } from '@/lib/apiClient';
import type { AttendanceBlock, AttendanceKind, CountItem, DashboardFilters, SurveyBlock } from '../types';

export const dashboardApi = {
  attendance: (kind: AttendanceKind, filters: DashboardFilters) =>
    apiClient.get<AttendanceBlock>(`/dashboard/${kind}`, { params: filters }).then((r) => r.data),
  courseResults: (filters: Pick<DashboardFilters, 'districtId' | 'unitId'>) =>
    apiClient.get<CountItem[]>('/dashboard/vocational/results', { params: filters }).then((r) => r.data),
  surveys: (filters: Pick<DashboardFilters, 'districtId' | 'unitId'>) =>
    apiClient.get<SurveyBlock>('/dashboard/surveys', { params: filters }).then((r) => r.data),
};
