import { apiClient } from '@/lib/apiClient';
import type { Deadline, DeadlineRequest } from '../types';

export const deadlineApi = {
  list: () => apiClient.get<Deadline[]>('/deadlines').then((r) => r.data),
  create: (r: DeadlineRequest) => apiClient.post<Deadline>('/deadlines', r).then((x) => x.data),
  update: (id: number, r: DeadlineRequest) => apiClient.put<Deadline>(`/deadlines/${id}`, r).then((x) => x.data),
  complete: (id: number) => apiClient.post<Deadline>(`/deadlines/${id}/complete`).then((x) => x.data),
  process: () => apiClient.post<{ sent: number }>('/deadlines/process').then((x) => x.data.sent),
};
