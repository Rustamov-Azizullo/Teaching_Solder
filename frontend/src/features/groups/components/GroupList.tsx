import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Popconfirm, Segmented, Space, Tag } from 'antd';
import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { NumberedTable, QueryBoundary } from '@/components/ui';
import { useCan } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { formatDate } from '@/utils/format';
import { useDeleteGroup, useGroups } from '../hooks/useGroups';
import { groupLabels, groupTypeLabels } from '../labels';
import type { GroupSummary, GroupType } from '../types';
import { GroupFormModal } from './GroupFormModal';

type Filter = GroupType | 'ALL';

const TYPE_PARAM = 'type';

/** Manzil qatoridagi `?type=` qiymatini filtrga aylantiradi (noto'g'ri qiymat — "Barchasi"). */
const toFilter = (value: string | null): Filter => (value !== null && value in groupTypeLabels ? (value as GroupType) : 'ALL');

export function GroupList() {
  const canCreate = useCan('groupWrite');
  const [searchParams, setSearchParams] = useSearchParams();
  const filter = toFilter(searchParams.get(TYPE_PARAM));
  const setFilter = (next: Filter) => setSearchParams(next === 'ALL' ? {} : { [TYPE_PARAM]: next }, { replace: true });
  const [isModalOpen, setModalOpen] = useState(false);
  const { data, isLoading, error, refetch } = useGroups(filter === 'ALL' ? undefined : filter);
  const { mutateAsync: deleteGroup, isPending: isDeleting } = useDeleteGroup();

  const handleDelete = async (id: number) => {
    try {
      await deleteGroup(id);
      notify.success(groupLabels.deleted);
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

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
          <NumberedTable<GroupSummary>
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
              ...(canCreate
                ? [{
                    title: '',
                    render: (_: unknown, row: GroupSummary) => (
                      <Popconfirm
                        title={groupLabels.confirmDelete}
                        okText={common.actions.delete}
                        cancelText={common.actions.cancel}
                        okButtonProps={{ danger: true, loading: isDeleting }}
                        onConfirm={() => handleDelete(row.id)}
                      >
                        <Button type="text" danger icon={<DeleteOutlined />} aria-label={common.actions.delete} />
                      </Popconfirm>
                    ),
                  }]
                : []),
            ]}
          />
        )}
      </QueryBoundary>
      <GroupFormModal open={isModalOpen} onClose={() => setModalOpen(false)} />
    </Space>
  );
}
