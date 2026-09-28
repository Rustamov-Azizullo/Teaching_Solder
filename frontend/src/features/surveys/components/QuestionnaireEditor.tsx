import { QueryBoundary } from '@/components/ui';
import { PageHeader } from '@/components/ui';
import { useMilitaryUnits } from '@/features/organization';
import { useSoldier } from '@/features/soldiers';
import { useFuturePlanMode, useQuestionnaire } from '../hooks/useQuestionnaire';
import { surveyLabels } from '../labels';
import { QuestionnaireForm } from './QuestionnaireForm';
import { QuestionnairePrintSheet } from './QuestionnairePrintSheet';

export function QuestionnaireEditor({ soldierId }: { soldierId: number }) {
  const soldier = useSoldier(soldierId);
  const questionnaire = useQuestionnaire(soldierId);
  const mode = useFuturePlanMode();
  const { data: units = [] } = useMilitaryUnits();

  return (
    <QueryBoundary
      isLoading={soldier.isLoading || questionnaire.isLoading || mode.isLoading}
      error={soldier.error ?? questionnaire.error ?? mode.error}
      data={soldier.data && mode.data && questionnaire.data !== undefined ? { soldier: soldier.data, mode: mode.data, questionnaire: questionnaire.data } : undefined}
    >
      {({ soldier: soldierData, mode: futurePlanMode, questionnaire: current }) => (
        <>
          <div className="no-print">
            <PageHeader title={soldierData.fullName} subtitle={`${surveyLabels.title} · ${soldierData.militaryUnitName}`} />
            <QuestionnaireForm
              key={current?.id ?? 'new'}
              soldierId={soldierId}
              unitId={soldierData.militaryUnitId}
              mode={futurePlanMode}
              questionnaire={current}
            />
          </div>
          {current?.status === 'FINALIZED' && (
            <QuestionnairePrintSheet
              soldier={soldierData}
              questionnaire={current}
              districtName={units.find((unit) => unit.id === soldierData.militaryUnitId)?.militaryDistrictName ?? '—'}
            />
          )}
        </>
      )}
    </QueryBoundary>
  );
}
