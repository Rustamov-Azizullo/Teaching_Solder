import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { employmentApi } from '../api/employmentApi';
import type { ExportRequest } from '../types';

export const useEmploymentPreview = () => useQuery({ queryKey: ['employment', 'preview'], queryFn: employmentApi.preview });
export const useEmploymentHistory = () => useQuery({ queryKey: ['employment', 'history'], queryFn: employmentApi.history });

export function useExportEmployment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: ExportRequest) => employmentApi.export(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['employment', 'history'] }),
  });
}
