import { EditOutlined } from '@ant-design/icons';
import { Button, Tabs } from 'antd';
import { useState } from 'react';
import { PageHeader, QueryBoundary } from '@/components/ui';
import { useCan } from '@/features/auth';
import { ResultsEditor } from '@/features/results';
import { LessonSchedule } from '@/features/schedule';
import { useGroup } from '../hooks/useGroups';
import { groupLabels } from '../labels';
import { GroupFormModal } from './GroupFormModal';
import { GroupInfo } from './GroupInfo';
import { GroupTeachersEditor } from './GroupTeachersEditor';
import { LeaderAssignModal } from './LeaderAssignModal';
import { MembersEditor } from './MembersEditor';

export function GroupDetail({ groupId }: { groupId: number }) {
  const canEdit = useCan('groupWrite');
  const canAssignLeader = useCan('leaderAssign');
  const canEditSchedule = useCan('scheduleWrite');
  const canSeeResults = useCan('resultRead');
  const { data, isLoading, error, refetch } = useGroup(groupId);
  const [isEditOpen, setEditOpen] = useState(false);
  const [isLeaderOpen, setLeaderOpen] = useState(false);

  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {(group) => (
        <>
          <PageHeader
            title={group.name}
            subtitle={group.militaryUnitName}
            actions={canEdit ? [<Button key="edit" icon={<EditOutlined />} onClick={() => setEditOpen(true)}>{groupLabels.editTitle}</Button>] : undefined}
          />
          <Tabs
            items={[
              {
                key: 'info',
                label: groupLabels.tabs.info,
                children: <GroupInfo group={group} canAssignLeader={canAssignLeader} onAssignLeader={() => setLeaderOpen(true)} />,
              },
              { key: 'members', label: `${groupLabels.tabs.members} (${group.members.length})`, children: <MembersEditor group={group} canEdit={canEdit} /> },
              { key: 'teachers', label: `${groupLabels.tabs.teachers} (${group.teachers.length})`, children: <GroupTeachersEditor group={group} canEdit={canEdit} /> },
              ...(group.type === 'VOCATIONAL' && canSeeResults
                ? [{ key: 'results', label: groupLabels.tabs.results, children: <ResultsEditor groupId={group.id} /> }]
                : []),
              {
                key: 'schedule',
                label: groupLabels.tabs.schedule,
                children: (
                  <LessonSchedule
                    groupId={group.id}
                    groupStart={group.startDate}
                    groupEnd={group.endDate}
                    canEdit={canEditSchedule}
                  />
                ),
              },
            ]}
          />
          <GroupFormModal open={isEditOpen} group={group} onClose={() => setEditOpen(false)} />
          <LeaderAssignModal group={group} open={isLeaderOpen} onClose={() => setLeaderOpen(false)} />
        </>
      )}
    </QueryBoundary>
  );
}
