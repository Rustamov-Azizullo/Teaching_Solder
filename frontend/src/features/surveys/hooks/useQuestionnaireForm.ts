import { Form } from 'antd';
import { useCallback } from 'react';
import { useDictionary } from '@/features/dictionaries';
import { getErrorMessage } from '@/lib/apiClient';
import { surveyLabels } from '../labels';
import type { FuturePlanMode, Questionnaire, QuestionnaireFormValues } from '../types';
import {
  OTM_ADMISSION_CODE,
  toPlanIds,
  toRequest,
  validateForFinalization,
} from '../utils/questionnaireMapper';
import { useSaveQuestionnaire } from './useQuestionnaire';
import { notify } from '@/lib/notify';

export function useQuestionnaireForm(soldierId: number, unitId: number, mode: FuturePlanMode) {
  const [form] = Form.useForm<QuestionnaireFormValues>();
  const planValue = Form.useWatch('futurePlanIds', form);
  const { data: directions = [] } = useDictionary('PROFESSION_DIRECTION', { unitId });
  const { data: plans = [] } = useDictionary('FUTURE_PLAN');
  const { mutateAsync, isPending } = useSaveQuestionnaire(soldierId);

  const includesHigherEducation = plans.some(
    (plan) => plan.code === OTM_ADMISSION_CODE && toPlanIds(planValue).includes(plan.id),
  );

  const save = useCallback(
    async (complete: boolean): Promise<Questionnaire | null> => {
      const values = form.getFieldsValue(true) as QuestionnaireFormValues;
      if (complete) {
        const problem = validateForFinalization(values, directions, plans, mode);
        if (problem) {
          notify.error(problem);
          return null;
        }
      }
      try {
        const saved = await mutateAsync(toRequest(values, includesHigherEducation, complete));
        notify.success(complete ? surveyLabels.finalized : surveyLabels.saved);
        return saved;
      } catch (error) {
        notify.error(getErrorMessage(error));
        return null;
      }
    },
    [form, directions, plans, mode, mutateAsync, includesHigherEducation],
  );

  return { form, includesHigherEducation, save, isSaving: isPending };
}
