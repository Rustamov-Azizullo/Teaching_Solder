import { Button, Form, Input, Select } from 'antd';
import { useEffect } from 'react';
import { QueryBoundary } from '@/components/ui';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { adminLabels } from '../labels';
import { useSettings, useUpdateSettings } from '../hooks/useAdmin';
import type { SettingsMap } from '../types';
import { notify } from '@/lib/notify';

const MODE_KEY = 'survey.futurePlan.mode';

export function SettingsForm() {
  const [form] = Form.useForm<SettingsMap>();
  const { data, isLoading, error, refetch } = useSettings();
  const { mutateAsync, isPending } = useUpdateSettings();

  useEffect(() => {
    if (data) form.setFieldsValue(data);
  }, [data, form]);

  const handleFinish = async (values: SettingsMap) => {
    try {
      await mutateAsync(values);
      notify.success(adminLabels.settings.saved);
    } catch (err) {
      notify.error(getErrorMessage(err));
    }
  };

  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {(settings) => (
        <Form form={form} layout="vertical" onFinish={handleFinish} style={{ maxWidth: 480 }}>
          {Object.keys(settings).map((key) => (
            <Form.Item key={key} name={key} label={adminLabels.settings.labels[key] ?? key}>
              {key === MODE_KEY ? (
                <Select options={Object.entries(adminLabels.settings.modes).map(([value, label]) => ({ value, label }))} />
              ) : (
                <Input />

              
              )}
            </Form.Item>
          ))}
          <Button type="primary" htmlType="submit" loading={isPending}>{common.actions.save}</Button>
        </Form>
      )}
    </QueryBoundary>
  );
}
