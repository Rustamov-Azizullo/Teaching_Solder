import { Button, Card, Form, Space } from 'antd';
import { useEffect } from 'react';
import { common } from '@/lib/i18n';
import { useSoldierForm } from '../hooks/useSoldierForm';
import { soldierLabels } from '../labels';
import type { Soldier, SourceLookup } from '../types';
import { EducationSection } from './EducationSection';
import { GeneralInfoSection } from './GeneralInfoSection';
import { SkillsSection } from './SkillsSection';

type SoldierFormProps = {
  pinfl: string;
  soldier?: Soldier;
  lookup?: SourceLookup;
  defaultUnitId?: number;
  onSaved: (soldier: Soldier) => void;
  onCancel: () => void;
};

export function SoldierForm({ pinfl, soldier, lookup, defaultUnitId, onSaved, onCancel }: SoldierFormProps) {
  const { form, initialValues, prefillFrom, handleValuesChange, sourceOf, submit, isSaving } = useSoldierForm(soldier, onSaved);

  useEffect(() => {
    form.setFieldsValue({ pinfl, noPriorOccupation: false, ...(defaultUnitId ? { militaryUnitId: defaultUnitId } : {}) });
    if (lookup) prefillFrom(lookup);
  }, [form, pinfl, lookup, prefillFrom, defaultUnitId]);

  return (
    <Form
      form={form}
      layout="vertical"
      initialValues={initialValues}
      onValuesChange={handleValuesChange}
      onFinish={submit}
      scrollToFirstError
      style={{ maxWidth: 760 }}
    >
      <Card title={soldierLabels.sections.general} style={{ marginBottom: 16 }}>
        <GeneralInfoSection sourceOf={sourceOf} />
      </Card>
      <Card title={soldierLabels.sections.education} style={{ marginBottom: 16 }}>
        <EducationSection sourceOf={sourceOf} />
      </Card>
      <Card title={soldierLabels.sections.skills} style={{ marginBottom: 16 }} extra={soldierLabels.pdfNote}>
        <SkillsSection />
      </Card>
      <Space wrap>
        <Button type="primary" htmlType="submit" loading={isSaving}>{common.actions.save}</Button>
        <Button onClick={onCancel}>{common.actions.cancel}</Button>
      </Space>
    </Form>
  );
}
