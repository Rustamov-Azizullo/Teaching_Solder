import { apiClient } from '@/lib/apiClient';
import type { FuturePlanMode, Questionnaire, QuestionnaireRequest } from '../types';

const NO_CONTENT = 204;

export const surveyApi = {
  get: async (soldierId: number): Promise<Questionnaire | null> => {
    const response = await apiClient.get<Questionnaire>(`/soldiers/${soldierId}/questionnaire`);
    return response.status === NO_CONTENT ? null : response.data;
  },
  save: (soldierId: number, request: QuestionnaireRequest) =>
    apiClient.put<Questionnaire>(`/soldiers/${soldierId}/questionnaire`, request).then((r) => r.data),
  futurePlanMode: () =>
    apiClient
      .get<Record<string, string>>('/settings')
      .then((r): FuturePlanMode => (r.data['survey.futurePlan.mode'] === 'SINGLE' ? 'SINGLE' : 'MULTIPLE')),
};
