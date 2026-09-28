import { Alert, Button, Card, Form, Input, InputNumber, Select, Space } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { adminLabels } from '../labels';
import { useSettings, useUpdateSettings } from '../hooks/useAdmin';
import { SETTING_GROUPS, TIME_PATTERN, type SettingField, type SettingGroupKey } from '../settingsSchema';
import type { SettingsMap } from '../types';

const labels = adminLabels.settings;
const MODE_OPTIONS = Object.entries(labels.modes).map(([value, label]) => ({ value, label }));

type FieldGroup = { group: SettingGroupKey; fields: SettingField[] };

function groupSettings(settings: SettingsMap): FieldGroup[] {
  const knownKeys = new Set(SETTING_GROUPS.flatMap(({ fields }) => fields.map((field) => field.key)));
  const groups: FieldGroup[] = SETTING_GROUPS.map(({ group, fields }): FieldGroup => ({
    group,
    fields: fields.filter((field) => field.key in settings),
  }));
  const otherFields = Object.keys(settings)
    .filter((key) => !knownKeys.has(key))
    .map((key): SettingField => ({ key, kind: 'text' }));
  const otherGroup: FieldGroup = { group: 'other', fields: otherFields };
  return [...groups, otherGroup].filter(({ fields }) => fields.length > 0);
}

function SettingInput({ kind }: { kind: SettingField['kind'] }) {
  if (kind === 'select') return <Select options={MODE_OPTIONS} />;
  if (kind === 'number') return <InputNumber min={0} style={{ width: '100%' }} stringMode />;
  if (kind === 'time') return <Input placeholder="HH:mm" maxLength={5} />;
  return <Input />;
}

function fieldRules(kind: SettingField['kind']) {
  if (kind === 'time') return [{ pattern: TIME_PATTERN, message: 'Vaqt HH:mm ko\'rinishida bo\'lishi kerak' }];
  return [{ required: true, message: common.fields.required }];
}

export function SettingsForm() {
  const [form] = Form.useForm<SettingsMap>();
  const [isDirty, setDirty] = useState(false);
  const { data, isLoading, error, refetch } = useSettings();
  const { mutateAsync, isPending } = useUpdateSettings();

  useEffect(() => {
    if (!data) return;
    form.setFieldsValue(data);
    setDirty(false);
  }, [data, form]);

  const groups = useMemo(() => (data ? groupSettings(data) : []), [data]);

  const handleFinish = async (values: SettingsMap) => {
    try {
      await mutateAsync(Object.fromEntries(Object.entries(values).map(([key, value]) => [key, String(value)])));
      notify.success(labels.saved);
    } catch (err) {
      notify.error(getErrorMessage(err));
    }
  };

  const handleReset = () => {
    form.setFieldsValue(data ?? {});
    setDirty(false);
  };

  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {() => (
        <Form form={form} layout="vertical" onFinish={handleFinish} onValuesChange={() => setDirty(true)} style={{ maxWidth: 640 }}>
          <Space direction="vertical" size="middle" style={{ display: 'flex' }}>
            {groups.map(({ group, fields }) => (
              <Card key={group} title={labels.groups[group]} size="small">
                {fields.map(({ key, kind }) => (
                  <Form.Item key={key} name={key} label={labels.labels[key] ?? key} rules={fieldRules(kind)}>
                    <SettingInput kind={kind} />
                  </Form.Item>
                ))}
              </Card>
            ))}
            {isDirty && <Alert type="info" showIcon message={labels.unsaved} />}
            <Space>
              <Button type="primary" htmlType="submit" loading={isPending} disabled={!isDirty}>{common.actions.save}</Button>
              <Button onClick={handleReset} disabled={!isDirty || isPending}>{labels.reset}</Button>
            </Space>
          </Space>
        </Form>
      )}
    </QueryBoundary>
  );
}
