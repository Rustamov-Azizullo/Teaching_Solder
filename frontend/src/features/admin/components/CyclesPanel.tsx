import { PlusOutlined } from '@ant-design/icons';
import { Button, Card, Popconfirm, Space, Tag } from 'antd';
import { NumberedTable, QueryBoundary } from '@/components/ui';
import { getErrorMessage } from '@/lib/apiClient';
import { notify } from '@/lib/notify';
import { formatDateTime } from '@/utils/format';
import { adminLabels } from '../labels';
import { useCycleActions, useCycles } from '../hooks/useAdmin';
import type { CycleRow } from '../types';

const t = adminLabels.cycles;

export function CyclesPanel() {
  const { data, isLoading, error, refetch } = useCycles();
  const { open, close, reopen } = useCycleActions();

  const run = async (action: () => Promise<unknown>) => {
    try { await action(); } catch (err) { notify.error(getErrorMessage(err)); }
  };
  const nextYear = (data?.[0]?.year ?? new Date().getFullYear()) + 1;

  return (
    <Card title={t.title} style={{ marginBottom: 16 }}>
      <Space direction="vertical" style={{ width: '100%' }}>
        <Button icon={<PlusOutlined />} loading={open.isPending} onClick={() => run(() => open.mutateAsync(nextYear))}>{`${t.open} (${nextYear})`}</Button>
        <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
          {(rows) => (
            <NumberedTable<CycleRow> rowKey="year" size="small" pagination={false} dataSource={rows}
              columns={[
                { title: t.year, dataIndex: 'year' },
                { title: t.status, dataIndex: 'status', render: (s: CycleRow['status']) => <Tag color={s === 'OPEN' ? 'green' : 'default'}>{s === 'OPEN' ? 'Ochiq' : 'Yopiq'}</Tag> },
                { title: t.opened, dataIndex: 'openedAt', render: formatDateTime },
                { title: t.closed, dataIndex: 'closedAt', render: formatDateTime },
                { title: '', render: (_: unknown, r) => (r.status === 'OPEN' ? (
                  <Popconfirm title={t.confirmClose} onConfirm={() => run(() => close.mutateAsync(r.year))}>
                    <Button size="small" danger>{t.close}</Button>
                  </Popconfirm>) : (
                  <Popconfirm title={t.confirmReopen} onConfirm={() => run(() => reopen.mutateAsync(r.year))}>
                    <Button size="small" loading={reopen.isPending && reopen.variables === r.year}>{t.reopen}</Button>
                  </Popconfirm>)) },
              ]} />
          )}
        </QueryBoundary>
      </Space>
    </Card>
  );
}
