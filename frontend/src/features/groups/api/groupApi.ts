import { apiClient } from '@/lib/apiClient';
import type {
  ContractRequest, Group, GroupRequest, GroupSummary, GroupType, Institution, InstitutionContract, InstitutionRequest,
  Teacher, TeacherRequest,
} from '../types';

export const groupApi = {
  list: (type?: GroupType) => apiClient.get<GroupSummary[]>('/groups', { params: { type } }).then((r) => r.data),
  get: (id: number) => apiClient.get<Group>(`/groups/${id}`).then((r) => r.data),
  create: (request: GroupRequest) => apiClient.post<Group>('/groups', request).then((r) => r.data),
  update: (id: number, request: GroupRequest) => apiClient.put<Group>(`/groups/${id}`, request).then((r) => r.data),
  remove: (id: number) => apiClient.delete(`/groups/${id}`).then(() => undefined),
  removeLeader: (id: number) => apiClient.delete<Group>(`/groups/${id}/leader`).then((r) => r.data),
  soldiersInOtherGroups: (id: number) =>
    apiClient.get<number[]>(`/groups/${id}/soldiers-in-other-groups`).then((r) => r.data),
  replaceMembers: (id: number, soldierIds: number[]) =>
    apiClient.put<Group>(`/groups/${id}/members`, { soldierIds }).then((r) => r.data),
  replaceTeachers: (id: number, teacherIds: number[]) =>
    apiClient.put<Group>(`/groups/${id}/teachers`, { teacherIds }).then((r) => r.data),
};

export const teacherApi = {
  list: () => apiClient.get<Teacher[]>('/teachers').then((r) => r.data),
  create: (request: TeacherRequest) => apiClient.post<Teacher>('/teachers', request).then((r) => r.data),
  update: (id: number, request: TeacherRequest) => apiClient.put<Teacher>(`/teachers/${id}`, request).then((r) => r.data),
  remove: (id: number) => apiClient.delete(`/teachers/${id}`).then(() => undefined),
  institutions: (unitId?: number) => apiClient.get<Institution[]>('/institutions', { params: { unitId } }).then((r) => r.data),
  createInstitution: (request: InstitutionRequest) =>
    apiClient.post<Institution>('/institutions', request).then((r) => r.data),
  updateInstitution: (id: number, request: InstitutionRequest) =>
    apiClient.put<Institution>(`/institutions/${id}`, request).then((r) => r.data),
  contracts: () => apiClient.get<InstitutionContract[]>('/institution-contracts').then((r) => r.data),
  createContract: (request: ContractRequest) =>
    apiClient.post<InstitutionContract>('/institution-contracts', request).then((r) => r.data),
  updateContract: (current: ContractRequest, next: ContractRequest) =>
    apiClient.put<InstitutionContract>('/institution-contracts', next, { params: current }).then((r) => r.data),
  removeContract: (request: ContractRequest) =>
    apiClient.delete('/institution-contracts', { params: request }).then(() => undefined),
  removeInstitution: (id: number) => apiClient.delete(`/institutions/${id}`).then(() => undefined),
};
