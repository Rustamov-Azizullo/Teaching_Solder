import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { surveyApi } from '../api/surveyApi';
import type { QuestionnaireRequest } from '../types';
import { queryKeys } from '@/lib/queryKeys';

export function useQuestionnaire(soldierId: number) {
  return useQuery({ queryKey: [...queryKeys.questionnaire, soldierId], queryFn: () => surveyApi.get(soldierId) });
}

export function useFuturePlanMode() {
  return useQuery({ queryKey: ['settings', 'futurePlanMode'], queryFn: surveyApi.futurePlanMode });
}

export function useSaveQuestionnaire(soldierId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: QuestionnaireRequest) => surveyApi.save(soldierId, request),
    onSuccess: (saved) => {
      queryClient.setQueryData([...queryKeys.questionnaire, soldierId], saved);
      queryClient.invalidateQueries({ queryKey: [...queryKeys.dashboard, 'surveys'] });
    },
  });
}
