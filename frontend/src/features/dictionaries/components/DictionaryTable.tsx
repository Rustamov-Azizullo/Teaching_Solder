import { EditOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Table, Tag } from 'antd';
import { useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { useCan } from '@/features/auth';
import { common } from '@/lib/i18n';
import { dictionaryLabels } from '../labels';
import { useDictionary } from '../hooks/useDictionary';
import type { DictionaryItem, DictionaryType } from '../types';
import { DictionaryItemModal } from './DictionaryItemModal';

export function DictionaryTable({ type }: { type: DictionaryType }) {
  const canWrite = useCan('dictionaryWrite');
  const { data, isLoading, error, refetch } = useDictionary(type, { activeOnly: false });
  const [editing, setEditing] = useState<DictionaryItem | null>(null);
  const [isModalOpen, setModalOpen] = useState(false);

  const openModal = (item: DictionaryItem | null) => {
    setEditing(item);
    setModalOpen(true);
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
                    width: 60,
                    render: (_: unknown, item: DictionaryItem) => (
                      <Button type="text" icon={<EditOutlined />} onClick={() => openModal(item)} aria-label={common.actions.edit} />
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
