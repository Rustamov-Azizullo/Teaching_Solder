import { Alert, Button, Input, Select, Space, Typography } from 'antd';
import { useMemo, useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { getErrorMessage } from '@/lib/apiClient';
import { notify } from '@/lib/notify';
import { useRolePermissionsEditor } from '../hooks/useRolePermissionsEditor';
import { rolePermissionsLabels } from '../labels';
import { PERMISSION_GROUPS, PERMISSION_GROUP_ORDER, type PermissionGroup } from '../utils/permissionGroups';
import { PERMISSION_PAGES } from '../utils/permissionPages';
import { RolePermissionsTable } from './RolePermissionsTable';
import { permissionLabels } from '@/features/auth';

export function RolePermissionsMatrix() {
  const editor = useRolePermissionsEditor();
  const [search, setSearch] = useState('');
  const [group, setGroup] = useState<PermissionGroup | undefined>(undefined);

  const filteredRows = useMemo(() => {
    const query = search.trim().toLowerCase();
    return (editor.rows ?? []).filter((row) => {
      if (group && PERMISSION_GROUPS[row.permission] !== group) return false;
      if (!query) return true;
      return `${permissionLabels[row.permission]} ${PERMISSION_PAGES[row.permission]}`.toLowerCase().includes(query);
    });
  }, [editor.rows, search, group]);

  const handleSave = async () => {
    try {
      await editor.save();
      notify.success(rolePermissionsLabels.saved);
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <QueryBoundary isLoading={editor.isLoading} error={editor.error} data={editor.rows} onRetry={editor.refetch}>
      {(allRows) => (
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <Alert type="info" showIcon message={rolePermissionsLabels.legend} />
          <Space wrap>
            <Input.Search
              allowClear
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder={rolePermissionsLabels.search}
              style={{ width: 320 }}
            />
            <Select<PermissionGroup>
              allowClear
              value={group}
              onChange={setGroup}
              placeholder={rolePermissionsLabels.allGroups}
              style={{ minWidth: 220 }}
              options={PERMISSION_GROUP_ORDER.map((key) => ({ value: key, label: rolePermissionsLabels.groups[key] }))}
            />
          </Space>
          <Space wrap>
            <Button type="primary" onClick={handleSave} loading={editor.isSaving} disabled={!editor.isDirty}>
              {rolePermissionsLabels.save}
            </Button>
            <Button onClick={editor.discard} disabled={!editor.isDirty || editor.isSaving}>{rolePermissionsLabels.discard}</Button>
            {editor.isDirty && <Typography.Text type="warning">{rolePermissionsLabels.unsaved}</Typography.Text>}
          </Space>
          <RolePermissionsTable rows={filteredRows} allRows={allRows} onToggle={editor.toggle} onToggleAll={editor.toggleAll} />
        </Space>
      )}
    </QueryBoundary>
  );
}
