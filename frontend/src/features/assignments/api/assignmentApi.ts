import { apiClient } from '@/lib/apiClient';
import type { Assignment, ContractRequest, DecisionRequest, ProposalRequest } from '../types';

export const assignmentApi = {
  list: () => apiClient.get<Assignment[]>('/assignments').then((r) => r.data),
  propose: (request: ProposalRequest) => apiClient.post<Assignment>('/assignments', request).then((r) => r.data),
  review: (id: number) => apiClient.post<Assignment>(`/assignments/${id}/review`).then((r) => r.data),
  decide: (id: number, request: DecisionRequest) =>
    apiClient.post<Assignment>(`/assignments/${id}/decision`, request).then((r) => r.data),
  contract: (id: number, request: ContractRequest) =>
    apiClient.put<Assignment>(`/assignments/${id}/contract`, request).then((r) => r.data),
};
