import { EditOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Space, Table, Tag } from 'antd';
import { useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { common } from '@/lib/i18n';
import { adminLabels } from '../labels';
import { useUsers } from '../hooks/useAdmin';
import type { UserRow } from '../types';
import { UserFormModal } from './UserFormModal';

export function UsersManager() {
  const { data, isLoading, error, refetch } = useUsers();
  const [editing, setEditing] = useState<UserRow | null>(null);
  const [isOpen, setOpen] = useState(false);

  const openModal = (user: UserRow | null) => {
    setEditing(user);
    setOpen(true);
  };

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal(null)}>{adminLabels.users.add}</Button>
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(users) => (
          <Table<UserRow>
            rowKey="id"
            dataSource={users}
            pagination={{ pageSize: 15, hideOnSinglePage: true }}
            scroll={{ x: 'max-content' }}
            columns={[
              { title: adminLabels.users.username, dataIndex: 'username' },
              { title: common.fields.fullName, dataIndex: 'fullName' },
              { title: adminLabels.users.role, dataIndex: 'roleLabel' },
              { title: common.fields.unit, dataIndex: 'militaryUnitName', render: (name: string | null) => name ?? '—' },
              { title: common.fields.status, dataIndex: 'active', render: (active: boolean) => <Tag color={active ? 'green' : 'red'}>{active ? common.fields.active : 'Bloklangan'}</Tag> },
              { title: '', render: (_: unknown, row) => <Button type="text" icon={<EditOutlined />} onClick={() => openModal(row)} aria-label={common.actions.edit} /> },
            ]}
          />
        )}
      </QueryBoundary>
      <UserFormModal user={editing} open={isOpen} onClose={() => setOpen(false)} />
    </Space>
  );
}
