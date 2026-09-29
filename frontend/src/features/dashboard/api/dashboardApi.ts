import { apiClient } from '@/lib/apiClient';
import type { DashboardFilters, Overview } from '../types';

export const dashboardApi = {
  overview: (filters: Pick<DashboardFilters, 'districtId' | 'unitId'>) =>
    apiClient.get<Overview>('/dashboard/overview', { params: filters }).then((r) => r.data),
};
