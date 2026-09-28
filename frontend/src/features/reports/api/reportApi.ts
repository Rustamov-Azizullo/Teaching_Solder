import { apiClient } from '@/lib/apiClient';
import { saveBlobResponse } from '@/lib/download';
import type { ReportParams } from '../types';

export async function downloadReport({ type, ...params }: ReportParams): Promise<void> {
  const response = await apiClient.get<Blob>(`/reports/${type}`, { params, responseType: 'blob' });
  saveBlobResponse(response, `${type.toLowerCase()}.${params.format.toLowerCase()}`);
}
