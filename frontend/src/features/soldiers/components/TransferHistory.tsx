import { Card } from 'antd';
import { NumberedTable, QueryBoundary } from '@/components/ui';
import { formatDateTime } from '@/utils/format';
import { useTransfers } from '../hooks/useSoldiers';
import { soldierLabels } from '../labels';
import type { TransferRecord } from '../types';

const t = soldierLabels.transfer;
const place = (unit: string, subdivision: string | null) => (subdivision ? `${unit} / ${subdivision}` : unit);

export function TransferHistory({ soldierId }: { soldierId: number }) {
  const { data, isLoading, error, refetch } = useTransfers(soldierId);
  return (
    <Card title={t.history} size="small">
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(records) => (
          <NumberedTable<TransferRecord>
            rowKey="id"
            size="small"
            pagination={false}
            locale={{ emptyText: t.empty }}
            scroll={{ x: 'max-content' }}
            dataSource={records}
            columns={[
              { title: t.at, dataIndex: 'transferredAt', render: formatDateTime },
              { title: t.from, render: (_: unknown, r) => place(r.fromUnit, r.fromSubdivision) },
              { title: t.to, render: (_: unknown, r) => place(r.toUnit, r.toSubdivision) },
              { title: t.reasonColumn, dataIndex: 'reason' },
              { title: t.by, dataIndex: 'transferredBy' },
            ]}
          />
        )}
      </QueryBoundary>
    </Card>
  );
}
