import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '@/lib/queryKeys';
import { resultApi } from '../api/resultApi';
import type { ResultInput } from '../types';

const key = (groupId: number) => ['results', groupId];

export const useResultsSheet = (groupId: number) => useQuery({ queryKey: key(groupId), queryFn: () => resultApi.sheet(groupId) });

export function useSaveResults(groupId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (entries: ResultInput[]) => resultApi.save(groupId, entries),
    onSuccess: (sheet) => queryClient.setQueryData(key(groupId), sheet),
  });
}

export function useApproveResults(groupId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => resultApi.approve(groupId),
    onSuccess: (sheet) => {
      queryClient.setQueryData(key(groupId), sheet);
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
      queryClient.invalidateQueries({ queryKey: queryKeys.groups });
    },
  });
}
