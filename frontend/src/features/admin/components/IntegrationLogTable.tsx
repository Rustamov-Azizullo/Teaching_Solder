import { Tag } from 'antd';
import { useState } from 'react';
import { NumberedTable, QueryBoundary } from '@/components/ui';
import { formatDateTime } from '@/utils/format';
import { adminLabels } from '../labels';
import { useIntegrationLogs } from '../hooks/useAdmin';
import type { IntegrationLogRow } from '../types';

const PAGE_SIZE = 10;
const t = adminLabels.integrations;

export function IntegrationLogTable() {
  const [page, setPage] = useState(0);
  const { data, isLoading, isFetching, error, refetch } = useIntegrationLogs(page, PAGE_SIZE);
  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {(result) => (
        <NumberedTable<IntegrationLogRow> rowKey="id" size="small" loading={isFetching} dataSource={result.content} scroll={{ x: 'max-content' }}
          pagination={{ current: result.page + 1, pageSize: PAGE_SIZE, total: result.totalElements, showSizeChanger: false, onChange: (p) => setPage(p - 1) }}
          columns={[
            { title: t.at, dataIndex: 'at', render: formatDateTime },
            { title: t.system, dataIndex: 'system' },
            { title: t.operation, dataIndex: 'operation' },
            { title: t.reference, dataIndex: 'reference' },
            { title: t.status, dataIndex: 'success', render: (ok: boolean) => <Tag color={ok ? 'green' : 'red'}>{ok ? 'OK' : 'Xato'}</Tag> },
            { title: t.message, dataIndex: 'message' },
            { title: t.duration, dataIndex: 'durationMs' },
            { title: t.actor, dataIndex: 'actor' },
          ]} />
      )}
    </QueryBoundary>
  );
}
