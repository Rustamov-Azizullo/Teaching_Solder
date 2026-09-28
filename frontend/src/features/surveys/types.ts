import type { NamedRef } from '@/types/api';

export type QuestionnaireStatus = 'DRAFT' | 'FINALIZED';

export type UniversityChoice = { priority: number; university: string; studyDirection: string };

export type Questionnaire = {
  id: number;
  soldierId: number;
  cycleYear: number;
  status: QuestionnaireStatus;
  psychologistName: string;
  filledDate: string;
  interestDirection: NamedRef | null;
  interestOtherText: string | null;
  futurePlans: NamedRef[];
  planOtherText: string | null;
  universityChoices: UniversityChoice[];
  specialtySubjects: NamedRef[];
  mandatorySubjects: NamedRef[];
  otherSubjectText: string | null;
};

export type QuestionnaireRequest = {
  interestDirectionId?: number;
  interestOtherText?: string;
  futurePlanIds: number[];
  planOtherText?: string;
  universityChoices: UniversityChoice[];
  specialtySubjectIds: number[];
  mandatorySubjectIds: number[];
  otherSubjectText?: string;
  complete: boolean;
};

export type QuestionnaireFormValues = {
  interestDirectionId?: number;
  interestOtherText?: string;
  futurePlanIds?: number[] | number;
  planOtherText?: string;
  universityChoices?: { university?: string; studyDirection?: string }[];
  specialtySubjectIds?: number[];
  mandatorySubjectIds?: number[];
  otherSubjectText?: string;
};

export type FuturePlanMode = 'SINGLE' | 'MULTIPLE';
