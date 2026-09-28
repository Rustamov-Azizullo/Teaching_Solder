import { PlusOutlined } from '@ant-design/icons';
import { Button, Modal, Space, Table, Tag } from 'antd';
import { useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { AttachmentPanel } from '@/features/attachments';
import { useCan } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { notify } from '@/lib/notify';
import { useAssignments, useReview } from '../hooks/useAssignments';
import { assignmentLabels, assignmentStatusLabels, directionLabels } from '../labels';
import type { Assignment, AssignmentStatus } from '../types';
import { ContractModal, DecisionModal, ProposeModal } from './AssignmentModals';

const COLORS: Record<AssignmentStatus, string> = { PROPOSED: 'blue', UNDER_REVIEW: 'gold', APPROVED: 'green', REJECTED: 'red' };

export function AssignmentList() {
  const canPropose = useCan('assignmentPropose');
  const canDecide = useCan('assignmentDecide');
  const canContract = useCan('groupWrite');
  const { data, isLoading, error, refetch } = useAssignments();
  const review = useReview();
  const [isProposeOpen, setProposeOpen] = useState(false);
  const [deciding, setDeciding] = useState<Assignment | null>(null);
  const [contracting, setContracting] = useState<Assignment | null>(null);
  const [viewing, setViewing] = useState<Assignment | null>(null);

  const startReview = async (id: number) => {
    try {
      await review.mutateAsync(id);
    } catch (err) {
      notify.error(getErrorMessage(err));
    }
  };

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      {canPropose && <Button type="primary" icon={<PlusOutlined />} onClick={() => setProposeOpen(true)}>{assignmentLabels.propose}</Button>}
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(items) => (
          <Table<Assignment>
            rowKey="id"
            dataSource={items}
            scroll={{ x: 'max-content' }}
            pagination={{ pageSize: 15, hideOnSinglePage: true }}
            columns={[
              { title: assignmentLabels.columns.unit, dataIndex: 'militaryUnitName' },
              { title: assignmentLabels.columns.institution, dataIndex: 'institutionName' },
              { title: assignmentLabels.columns.direction, dataIndex: 'direction', render: (d: Assignment['direction']) => directionLabels[d] },
              { title: assignmentLabels.columns.status, dataIndex: 'status', render: (s: AssignmentStatus) => <Tag color={COLORS[s]}>{assignmentStatusLabels[s]}</Tag> },
              { title: assignmentLabels.columns.basis, dataIndex: 'basisDocument', render: (v: string | null) => v ?? '—' },
              { title: assignmentLabels.columns.contract, dataIndex: 'contractNo', render: (v: string | null) => v ?? '—' },
              {
                title: '',
                render: (_: unknown, a) => (
                  <Space wrap>
                    {canDecide && a.status === 'PROPOSED' && <Button size="small" onClick={() => startReview(a.id)}>{assignmentLabels.review}</Button>}
                    {canDecide && (a.status === 'PROPOSED' || a.status === 'UNDER_REVIEW') && (
                      <Button size="small" type="primary" onClick={() => setDeciding(a)}>{assignmentLabels.decide}</Button>
                    )}
                    {canContract && a.status === 'APPROVED' && <Button size="small" onClick={() => setContracting(a)}>{assignmentLabels.contract}</Button>}
                    <Button size="small" type="link" onClick={() => setViewing(a)}>{assignmentLabels.files}</Button>
                  </Space>
                ),
              },
            ]}
          />
        )}
      </QueryBoundary>
      <ProposeModal open={isProposeOpen} onClose={() => setProposeOpen(false)} />
      <DecisionModal assignment={deciding} onClose={() => setDeciding(null)} />
      <ContractModal assignment={contracting} onClose={() => setContracting(null)} />
      <Modal open={viewing !== null} title={assignmentLabels.files} footer={null} onCancel={() => setViewing(null)} destroyOnHidden>
        {viewing && <AttachmentPanel ownerType="ASSIGNMENT" ownerId={viewing.id} title={`${viewing.militaryUnitName} — ${viewing.institutionName}`} />}
      </Modal>
    </Space>
  );
}
