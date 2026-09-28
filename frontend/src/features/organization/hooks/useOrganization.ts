import { useQuery } from '@tanstack/react-query';
import { organizationApi } from '../api/organizationApi';

const REFERENCE_STALE_TIME_MS = 5 * 60_000;

export function useRegions() {
  return useQuery({ queryKey: ['regions'], queryFn: organizationApi.regions, staleTime: REFERENCE_STALE_TIME_MS });
}

export function useTerritorialDistricts(regionId: number | undefined) {
  return useQuery({
    queryKey: ['regions', regionId, 'districts'],
    queryFn: () => organizationApi.districts(regionId as number),
    enabled: regionId !== undefined,
    staleTime: REFERENCE_STALE_TIME_MS,
  });
}

export function useMilitaryDistricts() {
  return useQuery({ queryKey: ['military-districts'], queryFn: organizationApi.militaryDistricts });
}

export function useMilitaryUnits() {
  return useQuery({ queryKey: ['military-units'], queryFn: organizationApi.militaryUnits });
}
