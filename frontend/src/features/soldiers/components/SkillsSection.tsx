import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Checkbox, Form, Input, Select, Space, Switch } from 'antd';
import { useDictionary } from '@/features/dictionaries';
import { awardPlaceLabels, soldierLabels } from '../labels';

const required = [{ required: true }];

type RepeatableProps = { name: string; title: string; addLabel: string; children: (field: number) => React.ReactNode };

function Repeatable({ name, title, addLabel, children }: RepeatableProps) {
  return (
    <Form.Item label={title}>
      <Form.List name={name}>
        {(fields, { add, remove }) => (
          <Space direction="vertical" style={{ width: '100%' }}>
            {fields.map((field) => (
              <Space key={field.key} align="start" wrap style={{ width: '100%' }}>
                {children(field.name)}
                <Button type="text" danger icon={<DeleteOutlined />} onClick={() => remove(field.name)} aria-label="O'chirish" />
              </Space>
            ))}
            <Button type="dashed" icon={<PlusOutlined />} onClick={() => add()}>{addLabel}</Button>
          </Space>
        )}
      </Form.List>
    </Form.Item>
  );
}

export function SkillsSection() {
  const form = Form.useFormInstance();
  const noPriorOccupation = Form.useWatch('noPriorOccupation', form) as boolean | undefined;
  const { data: languages = [] } = useDictionary('LANGUAGE');
  const { data: subjects = [] } = useDictionary('SUBJECT');
  const { data: awardKinds = [] } = useDictionary('AWARD_KIND');
  const f = soldierLabels.fields;
  const toOptions = (items: { id: number; name: string }[]) => items.map((item) => ({ value: item.id, label: item.name }));

  return (
    <>
      <Form.Item name="noPriorOccupation" valuePropName="checked" initialValue={false}>
        <Checkbox>{f.noPriorOccupation}</Checkbox>
      </Form.Item>
      {!noPriorOccupation && (
        <Form.Item name="priorOccupation" label={f.priorOccupation}><Input /></Form.Item>
      )}
      <Repeatable name="awards" title={f.awards} addLabel={f.addAward}>
        {(index) => (
          <>
            <Form.Item name={[index, 'kindId']} rules={required} noStyle>
              <Select placeholder={f.awardKind} style={{ minWidth: 240 }} options={toOptions(awardKinds)} />
            </Form.Item>
            <Form.Item name={[index, 'place']} rules={required} noStyle>
              <Select
                placeholder={f.awardPlace}
                style={{ minWidth: 120 }}
                options={Object.entries(awardPlaceLabels).map(([value, label]) => ({ value, label }))}
              />
            </Form.Item>
          </>
        )}
      </Repeatable>
      <Repeatable name="languageCertificates" title={f.languageCertificates} addLabel={f.addLanguageCertificate}>
        {(index) => (
          <>
            <Form.Item name={[index, 'dictionaryItemId']} rules={required} noStyle>
              <Select placeholder={f.language} style={{ minWidth: 180 }} options={toOptions(languages)} />
            </Form.Item>
            <Form.Item name={[index, 'level']} noStyle><Input placeholder={f.level} /></Form.Item>
          </>
        )}
      </Repeatable>
      <Repeatable name="professionCertificates" title={f.professionCertificates} addLabel={f.addProfessionCertificate}>
        {(index) => (
          <Form.Item name={[index, 'title']} rules={required} noStyle>
            <Input placeholder={f.professionTitle} style={{ minWidth: 260 }} />
          </Form.Item>
        )}
      </Repeatable>
      <Repeatable name="subjectCertificates" title={f.subjectCertificates} addLabel={f.addSubjectCertificate}>
        {(index) => (
          <Form.Item name={[index, 'dictionaryItemId']} rules={required} noStyle>
            <Select placeholder={f.subject} style={{ minWidth: 220 }} options={toOptions(subjects)} />
          </Form.Item>
        )}
      </Repeatable>
      <Form.Item name="trainable" label={f.trainable} valuePropName="checked">
        <Switch />
      </Form.Item>
    </>
  );
}
