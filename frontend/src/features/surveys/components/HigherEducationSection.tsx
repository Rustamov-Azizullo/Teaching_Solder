import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Form, Input, Select, Space, Typography } from 'antd';
import { useDictionary } from '@/features/dictionaries';
import { common } from '@/lib/i18n';
import { surveyLabels } from '../labels';
import { MAX_UNIVERSITY_CHOICES } from '../utils/questionnaireMapper';

/** V bo'lim: faqat 15-savolda "oliy ta'limga kirish" tanlanganda ko'rsatiladi. */
export function HigherEducationSection() {
  const { data: subjects = [] } = useDictionary('SUBJECT');
  const subjectOptions = subjects.map((subject) => ({ value: subject.id, label: subject.name }));

  return (
    <>
      <Typography.Paragraph strong>{surveyLabels.q16}</Typography.Paragraph>
      <Form.List name="universityChoices">
        {(fields, { add, remove }) => (
          <Space direction="vertical" style={{ width: '100%', marginBottom: 16 }}>
            {fields.map((field, index) => (
              <Space key={field.key} align="baseline" wrap style={{ width: '100%' }}>
                <Typography.Text strong>{surveyLabels.priority(index + 1)}</Typography.Text>
                <Form.Item name={[field.name, 'university']} noStyle>
                  <Input placeholder={surveyLabels.university} style={{ minWidth: 240 }} />
                </Form.Item>
                <Form.Item name={[field.name, 'studyDirection']} noStyle>
                  <Input placeholder={surveyLabels.studyDirection} style={{ minWidth: 240 }} />
                </Form.Item>
                {fields.length > 1 && (
                  <Button type="text" danger icon={<DeleteOutlined />} onClick={() => remove(field.name)} aria-label={common.actions.delete} />
                )}
              </Space>
            ))}
            {fields.length < MAX_UNIVERSITY_CHOICES && (
              <Button type="dashed" icon={<PlusOutlined />} onClick={() => add({})}>{surveyLabels.addChoice}</Button>
            )}
          </Space>
        )}
      </Form.List>
      <Form.Item name="specialtySubjectIds" label={surveyLabels.q17}>
        <Select mode="multiple" options={subjectOptions} optionFilterProp="label" />
      </Form.Item>
      <Form.Item name="mandatorySubjectIds" label={surveyLabels.q18}>
        <Select mode="multiple" options={subjectOptions} optionFilterProp="label" />
      </Form.Item>
    </>
  );
}
