import { apiClient } from '@/lib/apiClient';
import type { DashboardFilters, Overview, ScopedCatalog } from '../types';

export const dashboardApi = {
  overview: (filters: Pick<DashboardFilters, 'districtId' | 'unitId'>) =>
    apiClient.get<Overview>('/dashboard/overview', { params: filters }).then((r) => r.data),
  catalog: () => apiClient.get<ScopedCatalog>('/dashboard/catalog').then((r) => r.data),
};
