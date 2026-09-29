import { DeleteOutlined, EditOutlined } from '@ant-design/icons';
import { Button, Popconfirm, Tabs } from 'antd';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageHeader, QueryBoundary } from '@/components/ui';
import { useCan } from '@/features/auth';
import { ResultsEditor } from '@/features/results';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { useDeleteGroup, useGroup, useRemoveLeader } from '../hooks/useGroups';
import { groupLabels } from '../labels';
import { GroupFormModal } from './GroupFormModal';
import { GroupInfo } from './GroupInfo';
import { GroupTeachersEditor } from './GroupTeachersEditor';
import { MembersEditor } from './MembersEditor';

export function GroupDetail({ groupId }: { groupId: number }) {
  const canEdit = useCan('groupWrite');
  const canAssignLeader = useCan('leaderAssign');
  const canSeeResults = useCan('resultRead');
  const { data, isLoading, error, refetch } = useGroup(groupId);
  const navigate = useNavigate();
  const { mutateAsync: deleteGroup, isPending: isDeleting } = useDeleteGroup();
  const { mutateAsync: removeLeader } = useRemoveLeader(groupId);
  const [isEditOpen, setEditOpen] = useState(false);

  const handleDelete = async () => {
    try {
      await deleteGroup(groupId);
      notify.success(groupLabels.deleted);
      navigate('/groups');
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  const handleRemoveLeader = async () => {
    try {
      await removeLeader(undefined);
      notify.success(groupLabels.leaderRemoved);
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {(group) => (
        <>
          <PageHeader
            title={group.name}
            subtitle={group.militaryUnitName}
            actions={canEdit ? [
              <Button key="edit" icon={<EditOutlined />} onClick={() => setEditOpen(true)}>{groupLabels.editTitle}</Button>,
              <Popconfirm key="delete" title={groupLabels.confirmDelete} okText={common.actions.delete} cancelText={common.actions.cancel}
                okButtonProps={{ danger: true, loading: isDeleting }} onConfirm={handleDelete}>
                <Button danger icon={<DeleteOutlined />}>{common.actions.delete}</Button>
              </Popconfirm>,
            ] : undefined}
          />
          <Tabs
            items={[
              {
                key: 'info',
                label: groupLabels.tabs.info,
                children: <GroupInfo group={group} canAssignLeader={canAssignLeader} onRemoveLeader={handleRemoveLeader} />,
              },
              { key: 'members', label: `${groupLabels.tabs.members} (${group.members.length})`, children: <MembersEditor group={group} canEdit={canEdit} /> },
              { key: 'teachers', label: `${groupLabels.tabs.teachers} (${group.teachers.length})`, children: <GroupTeachersEditor group={group} canEdit={canEdit} /> },
              ...(group.type === 'VOCATIONAL' && canSeeResults
                ? [{ key: 'results', label: groupLabels.tabs.results, children: <ResultsEditor groupId={group.id} /> }]
                : []),
            ]}
          />
          <GroupFormModal open={isEditOpen} group={group} onClose={() => setEditOpen(false)} />
        </>
      )}
    </QueryBoundary>
  );
}
