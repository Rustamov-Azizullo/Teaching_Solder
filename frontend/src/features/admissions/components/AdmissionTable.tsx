import { CloudSyncOutlined, EditOutlined } from '@ant-design/icons';
import { Alert, Button, Space, Table, Tabs, Tag } from 'antd';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { QueryBoundary } from '@/components/ui';
import { useCan } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { notify } from '@/lib/notify';
import { formatDate } from '@/utils/format';
import { useAdmissions, useBmbaSync, useReserveList } from '../hooks/useAdmissions';
import { admissionLabels, onlineStatusLabels } from '../labels';
import type { AdmissionRow } from '../types';
import { AdmissionFunnel } from './AdmissionFunnel';
import { AdmissionModal } from './AdmissionModal';

const yes = (v: boolean) => <Tag color={v ? 'green' : 'default'}>{v ? 'Ha' : "Yo'q"}</Tag>;
const c = admissionLabels.columns;

function CandidateTable({ rows, canEdit, onEdit }: { rows: AdmissionRow[]; canEdit: boolean; onEdit: (r: AdmissionRow) => void }) {
  return (
    <Table<AdmissionRow> rowKey="soldierId" size="middle" dataSource={rows} scroll={{ x: 'max-content' }} pagination={{ pageSize: 15, hideOnSinglePage: true }}
      columns={[
        { title: c.name, dataIndex: 'fullName', render: (n: string, r) => <Link to={`/soldiers/${r.soldierId}`}>{n}</Link> },
        { title: c.unit, dataIndex: 'unitName' },
        { title: c.bmba, dataIndex: 'bmbaRegistered', render: yes },
        { title: c.benefits, dataIndex: 'benefitsUploaded', render: yes },
        { title: c.test, dataIndex: 'testParticipated', render: yes },
        { title: c.score, dataIndex: 'testScore', render: (v: number | null) => v ?? '—' },
        { title: c.admitted, dataIndex: 'admitted', render: yes },
        { title: c.university, dataIndex: 'university', render: (v: string | null) => v ?? '—' },
        { title: c.online, dataIndex: 'onlineStatus', render: (v: AdmissionRow['onlineStatus']) => onlineStatusLabels[v] },
        { title: c.end, dataIndex: 'serviceEndDate', render: formatDate },
        ...(canEdit ? [{ title: '', render: (_: unknown, r: AdmissionRow) => <Button type="text" icon={<EditOutlined />} onClick={() => onEdit(r)} aria-label={admissionLabels.edit} /> }] : []),
      ]}
    />
  );
}

export function AdmissionTable() {
  const canEdit = useCan('admissionWrite');
  const admissions = useAdmissions();
  const reserve = useReserveList();
  const sync = useBmbaSync();
  const [editing, setEditing] = useState<AdmissionRow | null>(null);

  const handleSync = async () => {
    try {
      notify.success(admissionLabels.synced(await sync.mutateAsync()));
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      {canEdit && <Button icon={<CloudSyncOutlined />} loading={sync.isPending} onClick={handleSync}>{admissionLabels.sync}</Button>}
      <AdmissionFunnel />
      <Tabs items={[
        { key: 'c', label: admissionLabels.tabs.candidates, children: (
          <QueryBoundary isLoading={admissions.isLoading} error={admissions.error} data={admissions.data} onRetry={admissions.refetch}>
            {(rows) => <CandidateTable rows={rows} canEdit={canEdit} onEdit={setEditing} />}
          </QueryBoundary>) },
        { key: 'r', label: admissionLabels.tabs.reserve, children: (
          <Space direction="vertical" style={{ width: '100%' }}>
            <Alert type="info" showIcon message={admissionLabels.reserveHint} />
            <QueryBoundary isLoading={reserve.isLoading} error={reserve.error} data={reserve.data} onRetry={reserve.refetch}>
              {(rows) => <CandidateTable rows={rows} canEdit={false} onEdit={setEditing} />}
            </QueryBoundary>
          </Space>) },
      ]} />
      <AdmissionModal row={editing} onClose={() => setEditing(null)} />
    </Space>
  );
}
