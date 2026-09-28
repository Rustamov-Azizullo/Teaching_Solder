import type { DictionaryItem } from '@/features/dictionaries';
import { surveyLabels } from '../labels';
import type { QuestionnaireFormValues } from '../types';
import {
  MAX_UNIVERSITY_CHOICES,
  OTM_ADMISSION_CODE,
  toRequest,
  validateForFinalization,
} from './questionnaireMapper';

const item = (id: number, code: string): DictionaryItem => ({
  id,
  type: 'FUTURE_PLAN' as DictionaryItem['type'],
  code,
  name: code,
  description: null,
  hours: null,
  active: true,
});

const directions = [item(1, 'IT'), item(2, 'OTHER')];
const plans = [item(10, 'WORK'), item(11, 'OTHER'), item(12, OTM_ADMISSION_CODE)];

const validValues = (overrides: QuestionnaireFormValues = {}): QuestionnaireFormValues => ({
  interestDirectionId: 1,
  futurePlanIds: [10],
  ...overrides,
});

const errors = surveyLabels.errors;

describe('validateForFinalization', () => {
  it('returns null for a valid non-OTM questionnaire', () => {
    expect(validateForFinalization(validValues(), directions, plans, 'MULTIPLE')).toBeNull();
  });

  it('requires an interest direction', () => {
    const result = validateForFinalization(validValues({ interestDirectionId: undefined }), directions, plans, 'MULTIPLE');
    expect(result).toBe(errors.direction);
  });

  it('requires text when the OTHER direction is chosen', () => {
    const values = validValues({ interestDirectionId: 2, interestOtherText: '   ' });
    expect(validateForFinalization(values, directions, plans, 'MULTIPLE')).toBe(errors.directionOther);
  });

  it('accepts the OTHER direction with text', () => {
    const values = validValues({ interestDirectionId: 2, interestOtherText: 'Farming' });
    expect(validateForFinalization(values, directions, plans, 'MULTIPLE')).toBeNull();
  });

  it('requires at least one plan', () => {
    const result = validateForFinalization(validValues({ futurePlanIds: [] }), directions, plans, 'MULTIPLE');
    expect(result).toBe(errors.plans);
  });

  it('allows only one plan in SINGLE mode', () => {
    const result = validateForFinalization(validValues({ futurePlanIds: [10, 11] }), directions, plans, 'SINGLE');
    expect(result).toBe(errors.single);
  });

  it('allows several plans in MULTIPLE mode', () => {
    const values = validValues({ futurePlanIds: [10, 11], planOtherText: 'x' });
    expect(validateForFinalization(values, directions, plans, 'MULTIPLE')).toBeNull();
  });

  it('accepts a single numeric plan id in SINGLE mode', () => {
    expect(validateForFinalization(validValues({ futurePlanIds: 10 }), directions, plans, 'SINGLE')).toBeNull();
  });

  it('requires text when the OTHER plan is chosen', () => {
    const result = validateForFinalization(validValues({ futurePlanIds: [11] }), directions, plans, 'MULTIPLE');
    expect(result).toBe(errors.planOther);
  });

  describe('when OTM admission is selected', () => {
    const otm = (overrides: QuestionnaireFormValues = {}) =>
      validValues({
        futurePlanIds: [12],
        universityChoices: [{ university: 'TATU', studyDirection: 'CS' }],
        specialtySubjectIds: [1],
        mandatorySubjectIds: [2],
        ...overrides,
      });

    it('passes with a choice and both subject groups', () => {
      expect(validateForFinalization(otm(), directions, plans, 'MULTIPLE')).toBeNull();
    });

    it('requires a complete university choice', () => {
      const values = otm({ universityChoices: [{ university: 'TATU' }, {}] });
      expect(validateForFinalization(values, directions, plans, 'MULTIPLE')).toBe(errors.choices);
    });

    it('requires specialty subjects', () => {
      const values = otm({ specialtySubjectIds: [] });
      expect(validateForFinalization(values, directions, plans, 'MULTIPLE')).toBe(errors.specialty);
    });

    it('requires mandatory subjects', () => {
      const values = otm({ mandatorySubjectIds: undefined });
      expect(validateForFinalization(values, directions, plans, 'MULTIPLE')).toBe(errors.mandatory);
    });
  });
});

describe('toRequest', () => {
  const higherEducationValues: QuestionnaireFormValues = {
    interestDirectionId: 1,
    futurePlanIds: 12,
    universityChoices: [
      { university: 'A', studyDirection: 'a' },
      { university: 'incomplete' },
      { university: 'B', studyDirection: 'b' },
      { university: 'C', studyDirection: 'c' },
      { university: 'D', studyDirection: 'd' },
    ],
    specialtySubjectIds: [1],
    mandatorySubjectIds: [2],
  };

  it('drops higher-education data when the OTM plan is not selected', () => {
    const request = toRequest(higherEducationValues, false, false);

    expect(request.universityChoices).toEqual([]);
    expect(request.specialtySubjectIds).toEqual([]);
    expect(request.mandatorySubjectIds).toEqual([]);
  });

  it('caps university choices at the maximum, skipping incomplete ones, with priorities', () => {
    const request = toRequest(higherEducationValues, true, true);

    expect(MAX_UNIVERSITY_CHOICES).toBe(3);
    expect(request.universityChoices).toEqual([
      { priority: 1, university: 'A', studyDirection: 'a' },
      { priority: 2, university: 'B', studyDirection: 'b' },
      { priority: 3, university: 'C', studyDirection: 'c' },
    ]);
    expect(request.specialtySubjectIds).toEqual([1]);
    expect(request.mandatorySubjectIds).toEqual([2]);
  });

  it('normalizes a single plan id to an array and passes the complete flag', () => {
    const request = toRequest(higherEducationValues, true, true);
    expect(request.futurePlanIds).toEqual([12]);
    expect(request.complete).toBe(true);
    expect(toRequest({}, false, false).futurePlanIds).toEqual([]);
  });
});
