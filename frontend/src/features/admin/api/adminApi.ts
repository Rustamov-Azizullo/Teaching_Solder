import { apiClient } from '@/lib/apiClient';
import type {
  AuditPage, CreateUserRequest, CycleRow, IntegrationPage, Location, LocationInput, RoleOption, SettingsMap, UpdateUserRequest,
  UserPermissionChange, UserPermissionState, UserRow,
} from '../types';

export const adminApi = {
  users: () => apiClient.get<UserRow[]>('/users').then((r) => r.data),
  roles: () => apiClient.get<RoleOption[]>('/users/roles').then((r) => r.data),
  createUser: (request: CreateUserRequest) => apiClient.post<UserRow>('/users', request).then((r) => r.data),
  updateUser: (id: number, request: UpdateUserRequest) => apiClient.put<UserRow>(`/users/${id}`, request).then((r) => r.data),
  deleteUser: (id: number) => apiClient.delete(`/users/${id}`).then(() => undefined),
  userPermissions: (id: number) =>
    apiClient.get<UserPermissionState[]>(`/users/${id}/permissions`).then((r) => r.data),
  updateUserPermissions: (id: number, changes: UserPermissionChange[]) =>
    apiClient.put<UserPermissionState[]>(`/users/${id}/permissions`, changes).then((r) => r.data),
  locations: () => apiClient.get<Location[]>('/locations').then((r) => r.data),
  createLocation: (input: LocationInput) => apiClient.post<Location>('/locations', input).then((r) => r.data),
  updateLocation: (id: number, input: LocationInput) => apiClient.put<Location>(`/locations/${id}`, input).then((r) => r.data),
  deleteLocation: (id: number) => apiClient.delete(`/locations/${id}`).then(() => undefined),
  audit: (params: { username: string; entity: string; page: number; size: number }) =>
    apiClient.get<AuditPage>('/audit-logs', { params }).then((r) => r.data),
  integrationLogs: (page: number, size: number) =>
    apiClient.get<IntegrationPage>('/integration-logs', { params: { page, size } }).then((r) => r.data),
  cycles: () => apiClient.get<CycleRow[]>('/cycles').then((r) => r.data),
  openCycle: (year: number) => apiClient.post<CycleRow>(`/cycles/${year}/open`).then((r) => r.data),
  closeCycle: (year: number) => apiClient.post<CycleRow>(`/cycles/${year}/close`).then((r) => r.data),
  runRetention: () => apiClient.post<{ warned: number; anonymized: number }>('/retention/run').then((r) => r.data),
  settings: () => apiClient.get<SettingsMap>('/settings').then((r) => r.data),
  updateSettings: (changes: SettingsMap) => apiClient.put<SettingsMap>('/settings', changes).then((r) => r.data),
};
