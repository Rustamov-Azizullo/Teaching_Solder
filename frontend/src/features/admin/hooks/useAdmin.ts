import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '../api/adminApi';
import type { CreateUserRequest, SettingsMap, UpdateUserRequest } from '../types';

export const useUsers = () => useQuery({ queryKey: ['users'], queryFn: adminApi.users });
export const useRoles = () => useQuery({ queryKey: ['roles'], queryFn: adminApi.roles, staleTime: Infinity });

export function useSaveUser(id?: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateUserRequest | UpdateUserRequest) =>
      id === undefined
        ? adminApi.createUser(request as CreateUserRequest)
        : adminApi.updateUser(id, request as UpdateUserRequest),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] }),
  });
}

export function useAuditLogs(params: { username: string; entity: string; page: number; size: number }) {
  return useQuery({ queryKey: ['audit', params], queryFn: () => adminApi.audit(params), placeholderData: keepPreviousData });
}

export function useIntegrationLogs(page: number, size: number) {
  return useQuery({ queryKey: ['integration-logs', page, size], queryFn: () => adminApi.integrationLogs(page, size), placeholderData: keepPreviousData });
}

export const useCycles = () => useQuery({ queryKey: ['cycles'], queryFn: adminApi.cycles });

export function useCycleActions() {
  const queryClient = useQueryClient();
  const refresh = () => queryClient.invalidateQueries({ queryKey: ['cycles'] });
  return {
    open: useMutation({ mutationFn: (year: number) => adminApi.openCycle(year), onSuccess: refresh }),
    close: useMutation({ mutationFn: (year: number) => adminApi.closeCycle(year), onSuccess: refresh }),
  };
}

export const useRunRetention = () => useMutation({ mutationFn: adminApi.runRetention });

export const useSettings = () => useQuery({ queryKey: ['settings'], queryFn: adminApi.settings });

export function useUpdateSettings() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (changes: SettingsMap) => adminApi.updateSettings(changes),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['settings'] }),
  });
}
