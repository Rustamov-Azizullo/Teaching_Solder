import { DeleteOutlined, EditOutlined, PlusOutlined, UserAddOutlined } from '@ant-design/icons';
import { Button, Popconfirm, Space, Table, Tag } from 'antd';
import { useMemo, useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { adminLabels } from '../labels';
import { useDeleteLocation, useLocations, useUsers } from '../hooks/useAdmin';
import { buildLocationTree, type LocationTreeNode } from '../utils/locationTree';
import type { Location, UserRow } from '../types';
import { LocationFormModal, type LocationFormTarget } from './LocationFormModal';
import { LocationUserModal } from './LocationUserModal';

const labels = adminLabels.locations;
/** Respublika rollari hududga biriktirilmaydi (`locationId = null`), shuning uchun ular ildiz qatorida ko'rsatiladi. */
const REPUBLIC_KEY = 'REPUBLIC';

function groupUsersByLocation(users: UserRow[]): Map<number | typeof REPUBLIC_KEY, UserRow[]> {
  const groups = new Map<number | typeof REPUBLIC_KEY, UserRow[]>();
  for (const user of users) {
    const key = user.locationId ?? REPUBLIC_KEY;
    groups.set(key, [...(groups.get(key) ?? []), user]);
  }
  return groups;
}

const LEVEL_COLORS = { REPUBLIC: 'gold', DISTRICT: 'blue', UNIT: 'green' } as const;

export function LocationsManager() {
  const { data, isLoading, error, refetch } = useLocations();
  const { mutateAsync: deleteLocation, isPending: isDeleting } = useDeleteLocation();
  const { data: users = [] } = useUsers();
  const [target, setTarget] = useState<LocationFormTarget | null>(null);
  const [userTarget, setUserTarget] = useState<Location | null>(null);
  const usersByLocation = useMemo(() => groupUsersByLocation(users), [users]);
  const tree = useMemo(() => buildLocationTree(data ?? []), [data]);

  const handleDelete = async (node: LocationTreeNode) => {
    try {
      await deleteLocation(node.id);
      notify.success(labels.deleted);
    } catch (err) {
      notify.error(getErrorMessage(err));
    }
  };

  const renderActions = (node: LocationTreeNode) => (
    <Space size={0} wrap>
      {node.level !== 'UNIT' && (
        <Button
          type="link"
          size="small"
          icon={<PlusOutlined />}
          onClick={() => setTarget({ mode: 'create', level: node.level === 'REPUBLIC' ? 'DISTRICT' : 'UNIT', parent: node })}
        >
          {node.level === 'REPUBLIC' ? labels.add : labels.addUnit}
        </Button>
      )}
      <Button type="link" size="small" icon={<UserAddOutlined />} onClick={() => setUserTarget(node)}>{labels.addUser}</Button>
      <Button type="text" icon={<EditOutlined />} onClick={() => setTarget({ mode: 'edit', location: node })} aria-label={common.actions.edit} />
      {node.level !== 'REPUBLIC' && (
        <Popconfirm
          title={labels.confirmDelete}
          okText={common.actions.delete}
          cancelText={common.actions.cancel}
          okButtonProps={{ danger: true, loading: isDeleting }}
          onConfirm={() => handleDelete(node)}
        >
          <Button type="text" danger icon={<DeleteOutlined />} aria-label={common.actions.delete} />
        </Popconfirm>
      )}
    </Space>
  );

  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {() => (
        <>
          <Table<LocationTreeNode>
            rowKey="id"
            size="middle"
            dataSource={tree}
            pagination={false}
            expandable={{ defaultExpandAllRows: true }}
            scroll={{ x: 'max-content' }}
            columns={[
              { title: labels.name, dataIndex: 'name' },
              {
                title: labels.level,
                dataIndex: 'level',
                width: 160,
                render: (level: LocationTreeNode['level']) => <Tag color={LEVEL_COLORS[level]}>{labels.levels[level]}</Tag>,
              },
              { title: labels.code, dataIndex: 'code', width: 140 },
              {
                title: labels.users,
                render: (_: unknown, node) =>
                  (usersByLocation.get(node.level === 'REPUBLIC' ? REPUBLIC_KEY : node.id) ?? []).map((user) => (
                    <Tag key={user.id} color={user.active ? undefined : 'red'}>{user.username}</Tag>
                  )),
              },
              { title: '', width: 380, render: (_: unknown, node) => renderActions(node) },
            ]}
          />
          <LocationFormModal target={target} onClose={() => setTarget(null)} />
          <LocationUserModal location={userTarget} onClose={() => setUserTarget(null)} />
        </>
      )}
    </QueryBoundary>
  );
}
