import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { queryKeys } from '@/lib/queryKeys';
import { dashboardApi } from '../api/dashboardApi';
import type { DashboardFilters } from '../types';

export function useOverview(filters: Pick<DashboardFilters, 'districtId' | 'unitId'>) {
  return useQuery({
    queryKey: [...queryKeys.dashboard, 'overview', filters],
    queryFn: () => dashboardApi.overview(filters),
    placeholderData: keepPreviousData,
  });
}
