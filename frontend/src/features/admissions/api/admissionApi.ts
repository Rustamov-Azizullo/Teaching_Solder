import { apiClient } from '@/lib/apiClient';
import type { AdmissionRow, AdmissionUpdate, FunnelStep, SyncResult } from '../types';

export const admissionApi = {
  list: () => apiClient.get<AdmissionRow[]>('/admissions').then((r) => r.data),
  update: (soldierId: number, update: AdmissionUpdate) => apiClient.put<AdmissionRow>(`/admissions/${soldierId}`, update).then((r) => r.data),
  sync: () => apiClient.post<SyncResult>('/admissions/bmba-sync').then((r) => r.data),
  funnel: () => apiClient.get<FunnelStep[]>('/admissions/funnel').then((r) => r.data),
  reserve: () => apiClient.get<AdmissionRow[]>('/admissions/reserve-list').then((r) => r.data),
};
