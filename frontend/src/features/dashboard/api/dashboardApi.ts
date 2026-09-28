import { apiClient } from '@/lib/apiClient';
import type { CountItem, DashboardFilters, DistrictSoldiers, GeographyBlock, SurveyBlock } from '../types';

export const dashboardApi = {
  courseResults: (filters: Pick<DashboardFilters, 'districtId' | 'unitId'>) =>
    apiClient.get<CountItem[]>('/dashboard/vocational/results', { params: filters }).then((r) => r.data),
  surveys: (filters: Pick<DashboardFilters, 'districtId' | 'unitId'>) =>
    apiClient.get<SurveyBlock>('/dashboard/surveys', { params: filters }).then((r) => r.data),
  geography: (filters: Pick<DashboardFilters, 'districtId' | 'unitId'>) =>
    apiClient.get<GeographyBlock>('/dashboard/geography', { params: filters }).then((r) => r.data),
  otmDistricts: (filters: Pick<DashboardFilters, 'districtId' | 'unitId'>) =>
    apiClient.get<DistrictSoldiers[]>('/dashboard/otm/geography', { params: filters }).then((r) => r.data),
};
