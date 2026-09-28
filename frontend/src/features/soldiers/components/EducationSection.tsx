import { Form, Select } from 'antd';
import { common } from '@/lib/i18n';
import { generalEducationLabels, higherEducationLabels, professionalEducationLabels, soldierLabels } from '../labels';
import type { FieldSource } from '../types';
import { FieldSourceTag } from './FieldSourceTag';

const toOptions = (labels: Record<string, string>) =>
  Object.entries(labels).map(([value, label]) => ({ value, label }));

export function EducationSection({ sourceOf }: { sourceOf: (field: string) => FieldSource | undefined }) {
  const f = soldierLabels.fields;
  return (
    <>
      <Form.Item
        name="generalEducation"
        label={<>{f.generalEducation}<FieldSourceTag source={sourceOf('generalEducation')} /></>}
        rules={[{ required: true, message: common.fields.required }]}
      >
        <Select options={toOptions(generalEducationLabels)} />
      </Form.Item>
      <Form.Item name="professionalEducation" label={f.professionalEducation}>
        <Select allowClear options={toOptions(professionalEducationLabels)} />
      </Form.Item>
      <Form.Item name="higherEducation" label={f.higherEducation}>
        <Select allowClear options={toOptions(higherEducationLabels)} />
      </Form.Item>
    </>
  );
}
