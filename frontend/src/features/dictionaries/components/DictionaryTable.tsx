import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Popconfirm, Space, Table, Tag } from 'antd';
import { useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { useCan } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { dictionaryLabels } from '../labels';
import { useDeleteDictionaryItem, useDictionary } from '../hooks/useDictionary';
import type { DictionaryItem, DictionaryType } from '../types';
import { DictionaryItemModal } from './DictionaryItemModal';

export function DictionaryTable({ type }: { type: DictionaryType }) {
  const canWrite = useCan('dictionaryWrite');
  const { data, isLoading, error, refetch } = useDictionary(type, { activeOnly: false });
  const { mutateAsync: deleteItem, isPending: isDeleting } = useDeleteDictionaryItem(type);
  const [editing, setEditing] = useState<DictionaryItem | null>(null);
  const [isModalOpen, setModalOpen] = useState(false);

  const openModal = (item: DictionaryItem | null) => {
    setEditing(item);
    setModalOpen(true);
  };

  const handleDelete = async (item: DictionaryItem) => {
    try {
      await deleteItem(item.id);
      notify.success(dictionaryLabels.deleted);
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <>
      {canWrite && (
        <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal(null)} style={{ marginBottom: 12 }}>
          {dictionaryLabels.add}
        </Button>
      )}
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(items) => (
          <Table<DictionaryItem>
            rowKey="id"
            size="middle"
            dataSource={items}
            pagination={false}
            scroll={{ x: 'max-content' }}
            columns={[
              { title: common.fields.name, dataIndex: 'name' },
              { title: common.fields.code, dataIndex: 'code', width: 180 },
              { title: dictionaryLabels.hours, dataIndex: 'hours', width: 90 },
              {
                title: common.fields.status,
                dataIndex: 'active',
                width: 110,
                render: (active: boolean) => <Tag color={active ? 'green' : 'default'}>{active ? common.fields.active : '—'}</Tag>,
              },
              ...(canWrite
                ? [{
                    title: '',
                    width: 100,
                    render: (_: unknown, item: DictionaryItem) => (
                      <Space size={0}>
                        <Button type="text" icon={<EditOutlined />} onClick={() => openModal(item)} aria-label={common.actions.edit} />
                        <Popconfirm
                          title={dictionaryLabels.confirmDelete}
                          okText={common.actions.delete}
                          cancelText={common.actions.cancel}
                          okButtonProps={{ danger: true, loading: isDeleting }}
                          onConfirm={() => handleDelete(item)}
                        >
                          <Button type="text" danger icon={<DeleteOutlined />} aria-label={common.actions.delete} />
                        </Popconfirm>
                      </Space>
                    ),
                  }]
                : []),
            ]}
          />
        )}
      </QueryBoundary>
      <DictionaryItemModal type={type} item={editing} open={isModalOpen} onClose={() => setModalOpen(false)} />
    </>
  );
}
