import { QuestionnaireEditor } from '@/features/surveys';
import { useIdParam } from '@/hooks/useIdParam';

export function QuestionnairePage() {
  return <QuestionnaireEditor soldierId={useIdParam('soldierId')} />;
}
