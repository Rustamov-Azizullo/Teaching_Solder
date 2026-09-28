import type { DictionaryItem } from '@/features/dictionaries';
import { surveyLabels } from '../labels';
import type {
  FuturePlanMode,
  Questionnaire,
  QuestionnaireFormValues,
  QuestionnaireRequest,
} from '../types';

const OTHER_CODE = 'OTHER';
export const OTM_ADMISSION_CODE = 'OTM_ADMISSION';
export const MAX_UNIVERSITY_CHOICES = 3;

export const toPlanIds = (value: QuestionnaireFormValues['futurePlanIds']): number[] =>
  value === undefined ? [] : Array.isArray(value) ? value : [value];

export function toFormValues(questionnaire: Questionnaire | null, mode: FuturePlanMode): QuestionnaireFormValues {
  if (!questionnaire) return { universityChoices: [{}] };
  const planIds = questionnaire.futurePlans.map((plan) => plan.id);
  return {
    interestDirectionId: questionnaire.interestDirection?.id,
    interestOtherText: questionnaire.interestOtherText ?? undefined,
    futurePlanIds: mode === 'SINGLE' ? planIds[0] : planIds,
    planOtherText: questionnaire.planOtherText ?? undefined,
    universityChoices: questionnaire.universityChoices.length > 0
      ? questionnaire.universityChoices.map(({ university, studyDirection }) => ({ university, studyDirection }))
      : [{}],
    specialtySubjectIds: questionnaire.specialtySubjects.map((subject) => subject.id),
    mandatorySubjectIds: questionnaire.mandatorySubjects.map((subject) => subject.id),
    otherSubjectText: questionnaire.otherSubjectText ?? undefined,
  };
}

export function toRequest(
  values: QuestionnaireFormValues,
  includesHigherEducation: boolean,
  complete: boolean,
): QuestionnaireRequest {
  const choices = includesHigherEducation
    ? (values.universityChoices ?? [])
        .filter((choice) => choice.university && choice.studyDirection)
        .slice(0, MAX_UNIVERSITY_CHOICES)
        .map((choice, index) => ({
          priority: index + 1,
          university: choice.university as string,
          studyDirection: choice.studyDirection as string,
        }))
    : [];
  return {
    interestDirectionId: values.interestDirectionId,
    interestOtherText: values.interestOtherText,
    futurePlanIds: toPlanIds(values.futurePlanIds),
    planOtherText: values.planOtherText,
    universityChoices: choices,
    specialtySubjectIds: includesHigherEducation ? values.specialtySubjectIds ?? [] : [],
    mandatorySubjectIds: includesHigherEducation ? values.mandatorySubjectIds ?? [] : [],
    otherSubjectText: values.otherSubjectText,
    complete,
  };
}

/** Yakunlashdan oldingi tekshiruv (server ham xuddi shu qoidalarni tekshiradi). Birinchi xato matnini qaytaradi. */
export function validateForFinalization(
  values: QuestionnaireFormValues,
  directions: DictionaryItem[],
  plans: DictionaryItem[],
  mode: FuturePlanMode,
): string | null {
  const errors = surveyLabels.errors;
  const direction = directions.find((item) => item.id === values.interestDirectionId);
  if (!direction) return errors.direction;
  if (direction.code === OTHER_CODE && !values.interestOtherText?.trim()) return errors.directionOther;

  const selectedPlans = plans.filter((plan) => toPlanIds(values.futurePlanIds).includes(plan.id));
  if (selectedPlans.length === 0) return errors.plans;
  if (mode === 'SINGLE' && selectedPlans.length > 1) return errors.single;
  if (selectedPlans.some((plan) => plan.code === OTHER_CODE) && !values.planOtherText?.trim()) return errors.planOther;

  if (selectedPlans.some((plan) => plan.code === OTM_ADMISSION_CODE)) {
    const hasChoice = (values.universityChoices ?? []).some((choice) => choice.university && choice.studyDirection);
    if (!hasChoice) return errors.choices;
    if (!values.specialtySubjectIds?.length) return errors.specialty;
    if (!values.mandatorySubjectIds?.length) return errors.mandatory;
  }
  return null;
}
