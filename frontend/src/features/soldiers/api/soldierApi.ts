import { apiClient } from '@/lib/apiClient';
import type { PageResponse } from '@/types/api';
import { saveBlobResponse } from '@/lib/download';
import type {
  FieldDiff, ImportResult, Soldier, SoldierRequest, SoldierSearchParams, SoldierSummary, SourceLookup, TransferRecord,
  TransferRequest,
} from '../types';

export const soldierApi = {
  search: (params: SoldierSearchParams) =>
    apiClient.get<PageResponse<SoldierSummary>>('/soldiers', { params }).then((r) => r.data),
  get: (id: number) => apiClient.get<Soldier>(`/soldiers/${id}`).then((r) => r.data),
  create: (request: SoldierRequest) => apiClient.post<Soldier>('/soldiers', request).then((r) => r.data),
  update: (id: number, request: SoldierRequest) =>
    apiClient.put<Soldier>(`/soldiers/${id}`, request).then((r) => r.data),
  transfers: (id: number) => apiClient.get<TransferRecord[]>(`/soldiers/${id}/transfers`).then((r) => r.data),
  transfer: (id: number, request: TransferRequest) =>
    apiClient.post<TransferRecord>(`/soldiers/${id}/transfer`, request).then((r) => r.data),
  refreshDiff: (id: number) => apiClient.post<FieldDiff[]>(`/soldiers/${id}/source-refresh`).then((r) => r.data),
  applyRefresh: (id: number, fields: string[]) => apiClient.put(`/soldiers/${id}/source-refresh`, fields).then(() => undefined),
  importFile: (file: File) => {
    const form = new FormData();
    form.append('file', file);
    return apiClient.post<ImportResult>('/soldiers/import', form).then((r) => r.data);
  },
  downloadTemplate: async () => {
    const response = await apiClient.get<Blob>('/soldiers/import/template', { responseType: 'blob' });
    saveBlobResponse(response, 'askarlar-shablon.xlsx');
  },
  lookup: (pinfl: string) =>
    apiClient.get<SourceLookup>('/soldiers/source-lookup', { params: { pinfl } }).then((r) => r.data),
};
