import { Alert, Button, Card, Space } from 'antd';
import { getErrorMessage } from '@/lib/apiClient';
import { notify } from '@/lib/notify';
import { adminLabels } from '../labels';
import { useRunRetention } from '../hooks/useAdmin';

const t = adminLabels.retention;

export function RetentionPanel() {
  const { mutateAsync, isPending } = useRunRetention();
  const run = async () => {
    try {
      const r = await mutateAsync();
      notify.success(t.result(r.warned, r.anonymized));
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };
  return (
    <Card title={t.title} style={{ marginBottom: 16 }}>
      <Space direction="vertical">
        <Alert type="info" showIcon message={t.hint} />
        <Button onClick={run} loading={isPending}>{t.run}</Button>
      </Space>
    </Card>
  );
}
