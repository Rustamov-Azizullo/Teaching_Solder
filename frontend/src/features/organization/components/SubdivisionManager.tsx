import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Input, Modal, Popconfirm, Space, Tree, Typography } from 'antd';
import type { DataNode } from 'antd/es/tree';
import { useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { useAuth, useCan } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { useSubdivisionMutations, useSubdivisions } from '../hooks/useSubdivisions';
import { organizationLabels } from '../labels';
import type { SubdivisionNode } from '../types';
import { MilitaryUnitSelect } from './MilitaryUnitSelect';

type Editing = { mode: 'create' | 'rename'; node: SubdivisionNode | null };

function Manager({ unitId }: { unitId: number }) {
  const canWrite = useCan('groupWrite');
  const { data, isLoading, error, refetch } = useSubdivisions(unitId);
  const { create, rename, remove } = useSubdivisionMutations(unitId);
  const [editing, setEditing] = useState<Editing | null>(null);
  const [name, setName] = useState('');
  const t = organizationLabels.subdivisions;

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
      notify.success(t.deleted);
    } catch (err) {
      notify.error(getErrorMessage(err));
    }
  };

  const toTree = (nodes: SubdivisionNode[]): DataNode[] =>
    nodes.map((node) => ({
      key: node.id,
      title: (
        <Space>
          <span>{node.name}</span>
          <Typography.Text type="secondary">({node.soldierCount})</Typography.Text>
          {canWrite && (
            <>
              <Button size="small" type="text" icon={<PlusOutlined />} onClick={() => open({ mode: 'create', node })} aria-label={t.addChild} />
              <Button size="small" type="text" icon={<EditOutlined />} onClick={() => open({ mode: 'rename', node })} aria-label={common.actions.edit} />
              <Popconfirm title={t.confirmDelete} onConfirm={() => handleDelete(node.id)}>
                <Button size="small" type="text" danger icon={<DeleteOutlined />} aria-label={common.actions.delete} />
              </Popconfirm>
            </>
          )}
        </Space>
      ),
      children: toTree(node.children),
    }));

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      {canWrite && (
        <Button type="primary" icon={<PlusOutlined />} onClick={() => open({ mode: 'create', node: null })}>{t.addRoot}</Button>
      )}
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch} isEmpty={(nodes) => nodes.length === 0}>
        {(nodes) => <Tree defaultExpandAll selectable={false} treeData={toTree(nodes)} />}
      </QueryBoundary>
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
  const [unitId, setUnitId] = useState<number | undefined>(user?.militaryUnitId ?? undefined);
  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <MilitaryUnitSelect value={unitId} onChange={setUnitId} />
      {unitId !== undefined && <Manager key={unitId} unitId={unitId} />}
    </Space>
  );
}
