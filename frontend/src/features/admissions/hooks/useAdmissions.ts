import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '@/lib/queryKeys';
import { admissionApi } from '../api/admissionApi';
import type { AdmissionUpdate } from '../types';

const KEY = ['admissions'];

export const useAdmissions = () => useQuery({ queryKey: KEY, queryFn: admissionApi.list });
export const useFunnel = (enabled = true) => useQuery({ queryKey: [...KEY, 'funnel'], queryFn: admissionApi.funnel, enabled });
export const useReserveList = () => useQuery({ queryKey: [...KEY, 'reserve'], queryFn: admissionApi.reserve });

function useRefreshingMutation<TVariables, TResult>(fn: (v: TVariables) => Promise<TResult>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: fn,
    onSuccess: () => Promise.all([queryClient.invalidateQueries({ queryKey: KEY }), queryClient.invalidateQueries({ queryKey: queryKeys.dashboard })]),
  });
}

export const useUpdateAdmission = () =>
  useRefreshingMutation(({ soldierId, update }: { soldierId: number; update: AdmissionUpdate }) => admissionApi.update(soldierId, update));
export const useBmbaSync = () => useRefreshingMutation(() => admissionApi.sync());
