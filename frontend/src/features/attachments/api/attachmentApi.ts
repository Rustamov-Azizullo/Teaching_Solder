import { apiClient } from '@/lib/apiClient';
import { saveBlobResponse } from '@/lib/download';
import type { Attachment, AttachmentOwnerType } from '../types';

export const attachmentApi = {
  list: (ownerType: AttachmentOwnerType, ownerId: number) =>
    apiClient.get<Attachment[]>('/attachments', { params: { ownerType, ownerId } }).then((r) => r.data),
  upload: (ownerType: AttachmentOwnerType, ownerId: number, file: File, kind?: string) => {
    const form = new FormData();
    form.append('file', file);
    return apiClient
      .post<Attachment>('/attachments', form, { params: { ownerType, ownerId, kind } })
      .then((r) => r.data);
  },
  download: async (attachment: Attachment) => {
    const response = await apiClient.get<Blob>(`/attachments/${attachment.id}/download`, { responseType: 'blob' });
    saveBlobResponse(response, attachment.fileName);
  },
  remove: (id: number) => apiClient.delete(`/attachments/${id}`).then(() => undefined),
};
