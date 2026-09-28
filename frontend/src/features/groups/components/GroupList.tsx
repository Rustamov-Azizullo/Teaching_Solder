import { PlusOutlined } from '@ant-design/icons';
import { Button, Segmented, Space, Table, Tag } from 'antd';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { QueryBoundary } from '@/components/ui';
import { useCan } from '@/features/auth';
import { formatDate } from '@/utils/format';
import { useGroups } from '../hooks/useGroups';
import { groupLabels, groupTypeLabels } from '../labels';
import type { GroupSummary, GroupType } from '../types';
import { GroupFormModal } from './GroupFormModal';

type Filter = GroupType | 'ALL';

export function GroupList() {
  const canCreate = useCan('groupWrite');
  const [filter, setFilter] = useState<Filter>('ALL');
  const [isModalOpen, setModalOpen] = useState(false);
  const { data, isLoading, error, refetch } = useGroups(filter === 'ALL' ? undefined : filter);

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Space wrap>
        <Segmented<Filter>
          value={filter}
          onChange={setFilter}
          options={[{ value: 'ALL', label: 'Barchasi' }, ...Object.entries(groupTypeLabels).map(([value, label]) => ({ value: value as GroupType, label }))]}
        />
        {canCreate && (
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>{groupLabels.create}</Button>
        )}
      </Space>
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(groups) => (
          <Table<GroupSummary>
            rowKey="id"
            dataSource={groups}
            pagination={false}
            scroll={{ x: 'max-content' }}
            columns={[
              { title: groupLabels.columns.name, dataIndex: 'name', render: (name: string, row) => <Link to={`/groups/${row.id}`}>{name}</Link> },
              {
                title: groupLabels.columns.type,
                dataIndex: 'type',
                render: (type: GroupType) => <Tag color={type === 'VOCATIONAL' ? 'blue' : 'purple'}>{groupTypeLabels[type]}</Tag>,
              },
              { title: groupLabels.columns.unit, dataIndex: 'militaryUnitName' },
              {
                title: groupLabels.columns.curriculum,
                render: (_: unknown, row) => row.professionName ?? row.subjectNames.join(', '),
              },
              {
                title: groupLabels.columns.period,
                render: (_: unknown, row) => `${formatDate(row.startDate)} — ${formatDate(row.endDate)}`,
              },
              { title: groupLabels.columns.members, dataIndex: 'memberCount' },
              { title: groupLabels.columns.leader, dataIndex: 'leaderName', render: (name: string | null) => name ?? groupLabels.noLeader },
            ]}
          />
        )}
      </QueryBoundary>
      <GroupFormModal open={isModalOpen} onClose={() => setModalOpen(false)} />
    </Space>
  );
}
