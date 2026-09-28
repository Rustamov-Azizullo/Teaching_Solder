import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { soldierApi } from '../api/soldierApi';
import type { SoldierRequest, SoldierSearchParams, TransferRequest } from '../types';
import { queryKeys } from '@/lib/queryKeys';

export function useSoldierSearch(params: SoldierSearchParams) {
  return useQuery({
    queryKey: [...queryKeys.soldiers, 'search', params],
    queryFn: () => soldierApi.search(params),
    placeholderData: keepPreviousData,
  });
}

export function useSoldier(id: number | undefined) {
  return useQuery({
    queryKey: [...queryKeys.soldiers, id],
    queryFn: () => soldierApi.get(id as number),
    enabled: id !== undefined,
  });
}

export function useSaveSoldier(id?: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: SoldierRequest) => (id === undefined ? soldierApi.create(request) : soldierApi.update(id, request)),
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: queryKeys.soldiers }),
        queryClient.invalidateQueries({ queryKey: queryKeys.groups }),
        queryClient.invalidateQueries({ queryKey: queryKeys.dashboard }),
      ]),
  });
}

export function useTransfers(id: number) {
  return useQuery({ queryKey: [...queryKeys.soldiers, id, 'transfers'], queryFn: () => soldierApi.transfers(id) });
}

export function useTransfer(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: TransferRequest) => soldierApi.transfer(id, request),
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: queryKeys.soldiers }),
        queryClient.invalidateQueries({ queryKey: queryKeys.groups }),
      ]),
  });
}

export function useSourceRefresh(id: number) {
  const queryClient = useQueryClient();
  return {
    diff: useMutation({ mutationFn: () => soldierApi.refreshDiff(id) }),
    apply: useMutation({
      mutationFn: (fields: string[]) => soldierApi.applyRefresh(id, fields),
      onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.soldiers }),
    }),
  };
}

export function useImportSoldiers() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (file: File) => soldierApi.importFile(file),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.soldiers }),
  });
}

export function useSourceLookup() {
  return useMutation({ mutationFn: (pinfl: string) => soldierApi.lookup(pinfl) });
}
