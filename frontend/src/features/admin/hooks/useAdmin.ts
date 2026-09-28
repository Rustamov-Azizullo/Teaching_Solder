import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '@/lib/queryKeys';
import { adminApi } from '../api/adminApi';
import type { CreateUserRequest, LocationInput, SettingsMap, UpdateUserRequest, UserPermissionChange } from '../types';

export const useUsers = () => useQuery({ queryKey: queryKeys.users, queryFn: adminApi.users });
export const useRoles = () => useQuery({ queryKey: ['roles'], queryFn: adminApi.roles, staleTime: Infinity });

export function useUpdateUser(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: UpdateUserRequest) => adminApi.updateUser(id, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.users }),
  });
}

export function useDeleteUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => adminApi.deleteUser(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.users }),
  });
}

export function useCreateUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateUserRequest) => adminApi.createUser(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.users }),
  });
}

const LOCATIONS_KEY = ['locations'] as const;

export const useLocations = () => useQuery({ queryKey: LOCATIONS_KEY, queryFn: adminApi.locations });

export function useSaveLocation(id?: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: LocationInput) => (id === undefined ? adminApi.createLocation(input) : adminApi.updateLocation(id, input)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: LOCATIONS_KEY }),
  });
}

export function useDeleteLocation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => adminApi.deleteLocation(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: LOCATIONS_KEY }),
  });
}

const userPermissionsKey = (id: number) => [...queryKeys.users, id, 'permissions'] as const;

export function useUserPermissions(id: number) {
  return useQuery({ queryKey: userPermissionsKey(id), queryFn: () => adminApi.userPermissions(id) });
}

export function useUpdateUserPermissions(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (changes: UserPermissionChange[]) => adminApi.updateUserPermissions(id, changes),
    onSuccess: (states) => {
      queryClient.setQueryData(userPermissionsKey(id), states);
      return queryClient.invalidateQueries({ queryKey: queryKeys.users, exact: true });
    },
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
