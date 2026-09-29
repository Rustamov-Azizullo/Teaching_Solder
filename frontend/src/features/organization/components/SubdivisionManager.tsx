import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Card, Empty, Input, Modal, Popconfirm, Space, Table, Tag } from 'antd';
import { useMemo, useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { useAuth, useCan } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { useLocationTree } from '../hooks/useOrganization';
import { useSubdivisionMutations, useSubdivisions } from '../hooks/useSubdivisions';
import { organizationLabels } from '../labels';
import { unitAncestry } from '../utils/districtUnitOptions';
import { countNodes, countSoldiers, pathNames, toRows, type SubdivisionRow } from '../utils/subdivisionTree';
import { DistrictUnitSelect, type DistrictUnitSelection } from './DistrictUnitSelect';
import { HierarchyPath } from './HierarchyPath';

type Editing = { mode: 'create' | 'rename'; node: SubdivisionRow | null };

const t = organizationLabels.subdivisions;
const NUMBER_WIDTH = 90;
const COUNT_WIDTH = 130;

function Manager({ unitId }: { unitId: number }) {
  const canWrite = useCan('groupWrite');
  const { data: locations = [] } = useLocationTree();
  const { data, isLoading, error, refetch } = useSubdivisions(unitId);
  const { create, rename, remove } = useSubdivisionMutations(unitId);
  const [editing, setEditing] = useState<Editing | null>(null);
  const [selectedId, setSelectedId] = useState<number | undefined>();
  const [name, setName] = useState('');
  const rows = useMemo(() => toRows(data ?? []), [data]);

  const open = (next: Editing) => {
    setEditing(next);
    setName(next.mode === 'rename' && next.node ? next.node.name : '');
  };

  const handleSave = async () => {
    if (!editing) return;
    try {
      if (editing.mode === 'create') {
        await create.mutateAsync({ name, parentId: editing.node?.id });
      } else if (editing.node) {
        await rename.mutateAsync({ id: editing.node.id, name, parentId: editing.node.parentId });
      }
      notify.success(common.states.saved);
      setEditing(null);
    } catch (err) {
      notify.error(getErrorMessage(err));
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await remove.mutateAsync(id);
      if (id === selectedId) setSelectedId(undefined);
      notify.success(t.deleted);
    } catch (err) {
      notify.error(getErrorMessage(err));
    }
  };

  const actions = (row: SubdivisionRow) => (
    <Space size={0} onClick={(event) => event.stopPropagation()}>
      <Button size="small" type="text" icon={<PlusOutlined />} onClick={() => open({ mode: 'create', node: row })} aria-label={t.addChild} />
      <Button size="small" type="text" icon={<EditOutlined />} onClick={() => open({ mode: 'rename', node: row })} aria-label={common.actions.edit} />
      <Popconfirm title={t.confirmDelete} onConfirm={() => handleDelete(row.id)}>
        <Button size="small" type="text" danger icon={<DeleteOutlined />} aria-label={common.actions.delete} />
      </Popconfirm>
    </Space>
  );

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Card size="small">
        <HierarchyPath levels={unitAncestry(locations, unitId)} subdivisions={pathNames(data ?? [], selectedId)} />
      </Card>
      <Card
        size="small"
        title={
          <Space wrap>
            <span>{t.tableTitle}</span>
            <Tag>{t.count(countNodes(data ?? []))}</Tag>
            <Tag color="blue">{t.soldiers(countSoldiers(data ?? []))}</Tag>
          </Space>
        }
        extra={canWrite && <Button type="primary" icon={<PlusOutlined />} onClick={() => open({ mode: 'create', node: null })}>{t.addRoot}</Button>}
      >
        <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch} isEmpty={(nodes) => nodes.length === 0}>
          {() => (
            <Table<SubdivisionRow>
              rowKey="id"
              size="middle"
              pagination={false}
              dataSource={rows}
              expandable={{ defaultExpandAllRows: true, indentSize: 24 }}
              rowClassName={(row) => (row.id === selectedId ? 'ant-table-row-selected' : '')}
              onRow={(row) => ({ onClick: () => setSelectedId(row.id === selectedId ? undefined : row.id), style: { cursor: 'pointer' } })}
              columns={[
                { title: '№', dataIndex: 'number', width: NUMBER_WIDTH },
                { title: t.columns.name, dataIndex: 'name' },
                { title: t.columns.children, key: 'children', width: COUNT_WIDTH, align: 'right', render: (_: unknown, row) => row.children?.length ?? 0 },
                { title: t.columns.soldiers, dataIndex: 'soldierCount', width: COUNT_WIDTH, align: 'right' },
                ...(canWrite ? [{ title: '', key: 'actions', width: 130, render: (_: unknown, row: SubdivisionRow) => actions(row) }] : []),
              ]}
            />
          )}
        </QueryBoundary>
      </Card>
      <Modal
        open={editing !== null}
        title={editing?.mode === 'rename' ? t.rename : editing?.node ? `${t.addChild}: ${editing.node.name}` : t.addRoot}
        onOk={handleSave}
        onCancel={() => setEditing(null)}
        okButtonProps={{ disabled: !name.trim(), loading: create.isPending || rename.isPending }}
        okText={common.actions.save}
        cancelText={common.actions.cancel}
      >
        <Input value={name} onChange={(e) => setName(e.target.value)} placeholder={t.namePlaceholder} onPressEnter={handleSave} autoFocus />
      </Modal>
    </Space>
  );
}

export function SubdivisionManager() {
  const { user } = useAuth();
  const [place, setPlace] = useState<DistrictUnitSelection>({ unitId: user?.militaryUnitId ?? undefined });
  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Card size="small" title={t.filterTitle}>
        <DistrictUnitSelect value={place} onChange={setPlace} />
      </Card>
      {place.unitId === undefined
        ? <Empty description={t.pickUnit} />
        : <Manager key={place.unitId} unitId={place.unitId} />}
    </Space>
  );
}
