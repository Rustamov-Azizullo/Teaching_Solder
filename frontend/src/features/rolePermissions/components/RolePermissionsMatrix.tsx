import { Button, Space, Typography } from 'antd';
import { QueryBoundary } from '@/components/ui';
import { getErrorMessage } from '@/lib/apiClient';
import { notify } from '@/lib/notify';
import { useRolePermissionsEditor } from '../hooks/useRolePermissionsEditor';
import { rolePermissionsLabels } from '../labels';
import { RolePermissionsTable } from './RolePermissionsTable';

export function RolePermissionsMatrix() {
  const editor = useRolePermissionsEditor();

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
      {(rows) => (
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <Space wrap>
            <Button type="primary" onClick={handleSave} loading={editor.isSaving} disabled={!editor.isDirty}>
              {rolePermissionsLabels.save}
            </Button>
            <Button onClick={editor.discard} disabled={!editor.isDirty || editor.isSaving}>{rolePermissionsLabels.discard}</Button>
            {editor.isDirty && <Typography.Text type="warning">{rolePermissionsLabels.unsaved}</Typography.Text>}
          </Space>
          <RolePermissionsTable rows={rows} onToggle={editor.toggle} />
        </Space>
      )}
    </QueryBoundary>
  );
}
