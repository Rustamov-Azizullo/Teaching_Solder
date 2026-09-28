import { apiClient } from '@/lib/apiClient';
import type { ResultInput, ResultsSheet } from '../types';

export const resultApi = {
  sheet: (groupId: number) => apiClient.get<ResultsSheet>(`/groups/${groupId}/results`).then((r) => r.data),
  save: (groupId: number, entries: ResultInput[]) =>
    apiClient.put<ResultsSheet>(`/groups/${groupId}/results`, { entries }).then((r) => r.data),
  approve: (groupId: number) => apiClient.post<ResultsSheet>(`/groups/${groupId}/results/approve`).then((r) => r.data),
};
