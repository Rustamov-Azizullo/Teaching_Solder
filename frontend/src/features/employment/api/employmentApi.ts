import { apiClient } from '@/lib/apiClient';
import { saveBlobResponse } from '@/lib/download';
import type { ExportRequest, HistoryRow, RegionGroup } from '../types';

export const employmentApi = {
  preview: () => apiClient.get<RegionGroup[]>('/employment/preview').then((r) => r.data),
  history: () => apiClient.get<HistoryRow[]>('/employment/history').then((r) => r.data),
  export: async (request: ExportRequest) => {
    const response = await apiClient.post<Blob>('/employment/export', request, { responseType: 'blob' });
    saveBlobResponse(response, `bandlik-royxati.${request.format.toLowerCase()}`);
  },
};
