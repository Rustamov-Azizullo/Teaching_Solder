import { apiClient } from '@/lib/apiClient';
import type {
  Group, GroupRequest, GroupSummary, GroupType, Institution, InstitutionRequest, LeaderOption, LeaderRequest,
  Teacher, TeacherRequest,
} from '../types';

export const groupApi = {
  list: (type?: GroupType) => apiClient.get<GroupSummary[]>('/groups', { params: { type } }).then((r) => r.data),
  get: (id: number) => apiClient.get<Group>(`/groups/${id}`).then((r) => r.data),
  create: (request: GroupRequest) => apiClient.post<Group>('/groups', request).then((r) => r.data),
  update: (id: number, request: GroupRequest) => apiClient.put<Group>(`/groups/${id}`, request).then((r) => r.data),
  assignLeader: (id: number, request: LeaderRequest) =>
    apiClient.put<Group>(`/groups/${id}/leader`, request).then((r) => r.data),
  replaceMembers: (id: number, soldierIds: number[]) =>
    apiClient.put<Group>(`/groups/${id}/members`, { soldierIds }).then((r) => r.data),
  replaceTeachers: (id: number, teacherIds: number[]) =>
    apiClient.put<Group>(`/groups/${id}/teachers`, { teacherIds }).then((r) => r.data),
  leaderOptions: (unitId: number) =>
    apiClient.get<LeaderOption[]>(`/military-units/${unitId}/group-leaders`).then((r) => r.data),
};

export const teacherApi = {
  list: () => apiClient.get<Teacher[]>('/teachers').then((r) => r.data),
  create: (request: TeacherRequest) => apiClient.post<Teacher>('/teachers', request).then((r) => r.data),
  update: (id: number, request: TeacherRequest) => apiClient.put<Teacher>(`/teachers/${id}`, request).then((r) => r.data),
  institutions: () => apiClient.get<Institution[]>('/institutions').then((r) => r.data),
  createInstitution: (request: InstitutionRequest) =>
    apiClient.post<Institution>('/institutions', request).then((r) => r.data),
};
