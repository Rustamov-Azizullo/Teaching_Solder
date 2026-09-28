import { apiClient } from '@/lib/apiClient';
import type { CountItem, DashboardFilters, SurveyBlock } from '../types';

export const dashboardApi = {
  courseResults: (filters: Pick<DashboardFilters, 'districtId' | 'unitId'>) =>
    apiClient.get<CountItem[]>('/dashboard/vocational/results', { params: filters }).then((r) => r.data),
  surveys: (filters: Pick<DashboardFilters, 'districtId' | 'unitId'>) =>
    apiClient.get<SurveyBlock>('/dashboard/surveys', { params: filters }).then((r) => r.data),
};
