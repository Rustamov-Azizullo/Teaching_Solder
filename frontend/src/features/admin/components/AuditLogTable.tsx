import { Input, Space } from 'antd';
import { useState } from 'react';
import { NumberedTable, QueryBoundary } from '@/components/ui';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';
import { formatDateTime } from '@/utils/format';
import { adminLabels } from '../labels';
import { useAuditLogs } from '../hooks/useAdmin';
import type { AuditLogRow } from '../types';

const PAGE_SIZE = 30;

export function AuditLogTable() {
  const [username, setUsername] = useState('');
  const [entity, setEntity] = useState('');
  const [page, setPage] = useState(0);
  const debouncedUsername = useDebouncedValue(username);
  const debouncedEntity = useDebouncedValue(entity);
  const { data, isLoading, isFetching, error, refetch } = useAuditLogs({ username: debouncedUsername, entity: debouncedEntity, page, size: PAGE_SIZE });

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Space wrap>
        <Input allowClear placeholder={adminLabels.audit.filterUser} onChange={(e) => { setUsername(e.target.value); setPage(0); }} />
        <Input allowClear placeholder={adminLabels.audit.filterEntity} onChange={(e) => { setEntity(e.target.value); setPage(0); }} />
      </Space>
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(result) => (
          <NumberedTable<AuditLogRow>
            rowKey="id"
            size="small"
            loading={isFetching}
            dataSource={result.content}
            scroll={{ x: 'max-content' }}
            pagination={{ current: result.page + 1, pageSize: PAGE_SIZE, total: result.totalElements, showSizeChanger: false, onChange: (p) => setPage(p - 1) }}
            columns={[
              { title: adminLabels.audit.at, dataIndex: 'at', render: formatDateTime },
              { title: adminLabels.audit.user, dataIndex: 'username' },
              { title: adminLabels.audit.action, dataIndex: 'action' },
              { title: adminLabels.audit.entity, render: (_: unknown, row) => `${row.entity}${row.entityId ? ` #${row.entityId}` : ''}` },
              { title: adminLabels.audit.details, dataIndex: 'details' },
            ]}
          />
        )}
      </QueryBoundary>
    </Space>
  );
}
