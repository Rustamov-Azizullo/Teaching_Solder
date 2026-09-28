import { EditOutlined, FormOutlined, SwapOutlined, SyncOutlined } from '@ant-design/icons';
import { Button, Space } from 'antd';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageHeader, QueryBoundary } from '@/components/ui';
import { AttachmentPanel } from '@/features/attachments';
import { useCan } from '@/features/auth';
import { useSoldier } from '../hooks/useSoldiers';
import { soldierLabels } from '../labels';
import { SoldierProfile } from './SoldierProfile';
import { SourceRefreshModal } from './SourceRefreshModal';
import { TransferHistory } from './TransferHistory';
import { TransferModal } from './TransferModal';

export function SoldierDetails({ soldierId }: { soldierId: number }) {
  const navigate = useNavigate();
  const canWrite = useCan('soldierWrite');
  const canTransfer = useCan('transfer');
  const canSeeQuestionnaire = useCan('questionnaireRead');
  const { data, isLoading, error, refetch } = useSoldier(soldierId);
  const [isTransferOpen, setTransferOpen] = useState(false);
  const [isRefreshOpen, setRefreshOpen] = useState(false);

  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {(soldier) => (
        <>
          <PageHeader
            title={soldier.fullName}
            subtitle={`${soldierLabels.profileTitle} · ${soldier.militaryUnitName}${soldier.subdivisionPath ? ` · ${soldier.subdivisionPath}` : ''}`}
            actions={[
              canSeeQuestionnaire && (
                <Button key="q" icon={<FormOutlined />} onClick={() => navigate(`/soldiers/${soldierId}/questionnaire`)}>
                  {soldierLabels.openQuestionnaire}
                </Button>
              ),
              canWrite && (
                <Button key="r" icon={<SyncOutlined />} onClick={() => setRefreshOpen(true)}>{soldierLabels.refresh.button}</Button>
              ),
              canTransfer && (
                <Button key="t" icon={<SwapOutlined />} onClick={() => setTransferOpen(true)}>{soldierLabels.transfer.button}</Button>
              ),
              canWrite && (
                <Button key="e" type="primary" icon={<EditOutlined />} onClick={() => navigate(`/soldiers/${soldierId}/edit`)}>
                  {soldierLabels.edit}
                </Button>
              ),
            ]}
          />
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <SoldierProfile soldier={soldier} />
            <AttachmentPanel ownerType="SOLDIER" ownerId={soldierId} />
            <TransferHistory soldierId={soldierId} />
          </Space>
          <TransferModal soldierId={soldierId} open={isTransferOpen} onClose={() => setTransferOpen(false)}
            onDone={() => { setTransferOpen(false); refetch(); }} />
          <SourceRefreshModal soldierId={soldierId} open={isRefreshOpen} onClose={() => setRefreshOpen(false)} />
        </>
      )}
    </QueryBoundary>
  );
}
