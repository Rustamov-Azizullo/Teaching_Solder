import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { attachmentApi } from '../api/attachmentApi';
import type { AttachmentOwnerType } from '../types';

export function useAttachments(ownerType: AttachmentOwnerType, ownerId: number) {
  return useQuery({ queryKey: ['attachments', ownerType, ownerId], queryFn: () => attachmentApi.list(ownerType, ownerId) });
}

export function useUploadAttachment(ownerType: AttachmentOwnerType, ownerId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ file, kind }: { file: File; kind?: string }) => attachmentApi.upload(ownerType, ownerId, file, kind),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['attachments', ownerType, ownerId] }),
  });
}

export function useRemoveAttachment(ownerType: AttachmentOwnerType, ownerId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => attachmentApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['attachments', ownerType, ownerId] }),
  });
}
