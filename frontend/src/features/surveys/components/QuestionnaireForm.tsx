import { CheckCircleOutlined, PrinterOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Form, Space, Tag } from 'antd';
import { useEffect } from 'react';
import { useCan } from '@/features/auth';
import { common } from '@/lib/i18n';
import { useQuestionnaireForm } from '../hooks/useQuestionnaireForm';
import { surveyLabels } from '../labels';
import type { FuturePlanMode, Questionnaire } from '../types';
import { toFormValues } from '../utils/questionnaireMapper';
import { HigherEducationSection } from './HigherEducationSection';
import { InterestSection } from './InterestSection';

type QuestionnaireFormProps = {
  soldierId: number;
  unitId: number;
  mode: FuturePlanMode;
  questionnaire: Questionnaire | null;
};

export function QuestionnaireForm({ soldierId, unitId, mode, questionnaire }: QuestionnaireFormProps) {
  const canWrite = useCan('questionnaireWrite');
  const { form, includesHigherEducation, save, isSaving } = useQuestionnaireForm(soldierId, unitId, mode);
  const isFinalized = questionnaire?.status === 'FINALIZED';

  useEffect(() => {
    form.setFieldsValue(toFormValues(questionnaire, mode));
  }, [form, questionnaire, mode]);

  return (
    <Form form={form} layout="vertical" disabled={!canWrite} style={{ maxWidth: 760 }}>
      <Space style={{ marginBottom: 12 }} wrap>
        {questionnaire && (
          <Tag color={isFinalized ? 'green' : 'gold'}>{surveyLabels.status[questionnaire.status]}</Tag>
        )}
        {questionnaire && <span>{surveyLabels.header.psychologist}: {questionnaire.psychologistName}</span>}
      </Space>
      {isFinalized && <Alert type="info" showIcon message={surveyLabels.noteFinalized} style={{ marginBottom: 16 }} />}
      <Card title={surveyLabels.sections.interests} style={{ marginBottom: 16 }}>
        <InterestSection unitId={unitId} mode={mode} />
      </Card>
      <Card title={surveyLabels.sections.higher} style={{ marginBottom: 16 }}>
        {includesHigherEducation ? (
          <HigherEducationSection />
        ) : (
          <Alert type="info" message={surveyLabels.sections.higherLocked} />
        )}
      </Card>
      <Space wrap className="no-print">
        {canWrite && (
          <>
            <Button onClick={() => save(false)} loading={isSaving}>{common.actions.saveDraft}</Button>
            <Button type="primary" icon={<CheckCircleOutlined />} onClick={() => save(true)} loading={isSaving}>
              {surveyLabels.actions.finalize}
            </Button>
          </>
        )}
        {isFinalized && (
          <Button icon={<PrinterOutlined />} onClick={() => window.print()} disabled={false}>
            {surveyLabels.actions.print}
          </Button>
        )}
      </Space>
    </Form>
  );
}
