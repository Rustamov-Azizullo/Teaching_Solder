import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/lib/apiClient';

export type Suggestion = { kind: 'DIRECTION' | 'SUBJECT'; label: string; soldiers: { id: number; fullName: string }[] };

export function useSuggestions(unitId: number | undefined) {
  return useQuery({
    queryKey: ['group-suggestions', unitId],
    queryFn: () => apiClient.get<Suggestion[]>('/surveys/group-suggestions', { params: { unitId } }).then((r) => r.data),
  });
}
