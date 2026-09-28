import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { facilityApi } from '../api/facilityApi';
import type { FacilityRequest } from '../types';

const KEY = ['facilities'];

export const useFacilities = () => useQuery({ queryKey: KEY, queryFn: facilityApi.list });

export function useSaveFacility(id?: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: FacilityRequest) => (id === undefined ? facilityApi.create(request) : facilityApi.update(id, request)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: KEY }),
  });
}

export function useRemoveFacility() {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: (id: number) => facilityApi.remove(id), onSuccess: () => queryClient.invalidateQueries({ queryKey: KEY }) });
}
