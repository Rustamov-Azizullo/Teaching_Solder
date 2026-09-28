import { Alert, Checkbox, Form, Input, Radio, Space } from 'antd';
import { useDictionary } from '@/features/dictionaries';
import { common } from '@/lib/i18n';
import { surveyLabels } from '../labels';
import type { FuturePlanMode } from '../types';
import { toPlanIds } from '../utils/questionnaireMapper';

type InterestSectionProps = { unitId: number; mode: FuturePlanMode };

export function InterestSection({ unitId, mode }: InterestSectionProps) {
  const form = Form.useFormInstance();
  const directionId = Form.useWatch('interestDirectionId', form) as number | undefined;
  const planValue = Form.useWatch('futurePlanIds', form);
  const { data: directions = [] } = useDictionary('PROFESSION_DIRECTION', { unitId });
  const { data: plans = [] } = useDictionary('FUTURE_PLAN');

  const isOtherDirection = directions.find((item) => item.id === directionId)?.code === 'OTHER';
  const hasOtherPlan = plans.some((plan) => plan.code === 'OTHER' && toPlanIds(planValue).includes(plan.id));
  const planOptions = plans.map((plan) => ({ value: plan.id, label: plan.name }));

  return (
    <>
      <Form.Item name="interestDirectionId" label={surveyLabels.q14}>
        {directions.length === 0 ? (
          <Alert type="warning" message={surveyLabels.noDirections} />
        ) : (
          <Radio.Group>
            <Space direction="vertical">
              {directions.map((direction) => (
                <Radio key={direction.id} value={direction.id}>{direction.name}</Radio>
              ))}
            </Space>
          </Radio.Group>
        )}
      </Form.Item>
      {isOtherDirection && (
        <Form.Item name="interestOtherText" label={`${common.other}: ${surveyLabels.otherText}`}><Input /></Form.Item>
      )}
      <Form.Item name="futurePlanIds" label={surveyLabels.q15}>
        {mode === 'SINGLE' ? (
          <Radio.Group>
            <Space direction="vertical">
              {planOptions.map((option) => <Radio key={option.value} value={option.value}>{option.label}</Radio>)}
            </Space>
          </Radio.Group>
        ) : (
          <Checkbox.Group style={{ display: 'grid', gap: 8 }} options={planOptions} />
        )}
      </Form.Item>
      {hasOtherPlan && (
        <Form.Item name="planOtherText" label={`${common.other}: ${surveyLabels.otherText}`}><Input /></Form.Item>
      )}
    </>
  );
}

