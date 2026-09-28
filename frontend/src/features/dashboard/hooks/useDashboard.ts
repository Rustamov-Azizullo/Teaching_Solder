import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { dashboardApi } from '../api/dashboardApi';
import type { AttendanceKind, DashboardFilters } from '../types';
import { queryKeys } from '@/lib/queryKeys';

export function useAttendanceBlock(kind: AttendanceKind, filters: DashboardFilters, enabled: boolean) {
  return useQuery({
    queryKey: [...queryKeys.dashboard, kind, filters],
    queryFn: () => dashboardApi.attendance(kind, filters),
    enabled,
    placeholderData: keepPreviousData,
  });
}

export function useCourseResults(filters: Pick<DashboardFilters, 'districtId' | 'unitId'>) {
  return useQuery({
    queryKey: [...queryKeys.dashboard, 'course-results', filters],
    queryFn: () => dashboardApi.courseResults(filters),
    placeholderData: keepPreviousData,
  });
}

export function useSurveyBlock(filters: Pick<DashboardFilters, 'districtId' | 'unitId'>, enabled: boolean) {
  return useQuery({
    queryKey: [...queryKeys.dashboard, 'surveys', filters],
    queryFn: () => dashboardApi.surveys(filters),
    enabled,
    placeholderData: keepPreviousData,
  });
}
