import { DeleteOutlined, EditOutlined } from '@ant-design/icons';
import { Button, Popconfirm, Space, Tag, Typography } from 'antd';
import { useState } from 'react';
import { NumberedTable, QueryBoundary } from '@/components/ui';
import { useAuth } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { adminLabels } from '../labels';
import { useDeleteUser, useUsers } from '../hooks/useAdmin';
import type { UserRow } from '../types';
import { UserFormModal } from './UserFormModal';

export function UsersManager() {
  const { data, isLoading, error, refetch } = useUsers();
  const { user: viewer } = useAuth();
  const { mutateAsync: deleteUser, isPending: isDeleting } = useDeleteUser();
  const [editing, setEditing] = useState<UserRow | null>(null);

  const handleDelete = async (id: number) => {
    try {
      await deleteUser(id);
      notify.success(adminLabels.users.deleted);
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Typography.Text type="secondary">{adminLabels.users.addHint}</Typography.Text>
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(users) => (
          <NumberedTable<UserRow>
            rowKey="id"
            dataSource={users}
            pagination={{ defaultPageSize: 15, showSizeChanger: true, pageSizeOptions: [10, 15, 30, 50], showTotal: (total) => `Jami: ${total}` }}
            scroll={{ x: 'max-content' }}
            columns={[
              { title: adminLabels.users.username, dataIndex: 'username' },
              { title: common.fields.fullName, dataIndex: 'fullName' },
              { title: adminLabels.users.role, dataIndex: 'roleLabel' },
              { title: adminLabels.users.location, dataIndex: 'locationName', render: (name: string | null) => name ?? '—' },
              { title: common.fields.status, dataIndex: 'active', render: (active: boolean) => <Tag color={active ? 'green' : 'red'}>{active ? common.fields.active : 'Bloklangan'}</Tag> },
              {
                title: '',
                render: (_: unknown, row) => (
                  <Space size={0}>
                    <Button type="text" icon={<EditOutlined />} onClick={() => setEditing(row)} aria-label={common.actions.edit} />
                    {row.id !== viewer?.id && (
                      <Popconfirm
                        title={adminLabels.users.confirmDelete}
                        okText={common.actions.delete}
                        cancelText={common.actions.cancel}
                        okButtonProps={{ danger: true, loading: isDeleting }}
                        onConfirm={() => handleDelete(row.id)}
                      >
                        <Button type="text" danger icon={<DeleteOutlined />} aria-label={common.actions.delete} />
                      </Popconfirm>
                    )}
                  </Space>
                ),
              },
            ]}
          />
        )}
      </QueryBoundary>
      <UserFormModal user={editing} onClose={() => setEditing(null)} />
    </Space>
  );
}
