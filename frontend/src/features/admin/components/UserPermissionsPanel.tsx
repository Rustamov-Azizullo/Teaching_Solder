import { Button, Checkbox, Divider, List, Space, Tag, Typography } from 'antd';
import { QueryBoundary } from '@/components/ui';
import { permissionLabels } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { notify } from '@/lib/notify';
import { useUserPermissionsEditor, type UserPermissionRow } from '../hooks/useUserPermissionsEditor';
import { adminLabels } from '../labels';

const t = adminLabels.userPermissions;

type PermissionItemProps = { row: UserPermissionRow; onToggle: (granted: boolean) => void };

function PermissionItem({ row, onToggle }: PermissionItemProps) {
  return (
    <List.Item style={{ paddingBlock: 4 }}>
      <Checkbox checked={row.isChecked} disabled={row.isLocked} onChange={(event) => onToggle(event.target.checked)}>
        {permissionLabels[row.permission]}
      </Checkbox>
      {row.grantedByRole && <Tag>{t.byRole}</Tag>}
    </List.Item>
  );
}

/** Mavjud ADMIN/USER foydalanuvchining shaxsiy ruxsatlari (faqat SuperAdmin/Mega SuperAdmin uchun ko'rsatiladi). */
export function UserPermissionsPanel({ userId }: { userId: number }) {
  const editor = useUserPermissionsEditor(userId);

  const handleSave = async () => {
    try {
      await editor.save();
      notify.success(t.saved);
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <>
      <Divider orientation="left">{t.title}</Divider>
      <Typography.Paragraph type="secondary">{t.hint}</Typography.Paragraph>
      <QueryBoundary isLoading={editor.isLoading} error={editor.error} data={editor.rows} onRetry={editor.refetch}>
        {(rows) => (
          <Space direction="vertical" style={{ width: '100%' }}>
            <List
              size="small"
              dataSource={rows}
              rowKey="permission"
              renderItem={(row) => (
                <PermissionItem row={row} onToggle={(granted) => editor.toggle(row.permission, granted)} />
              )}
            />
            <Button onClick={handleSave} loading={editor.isSaving} disabled={!editor.isDirty}>{t.save}</Button>
          </Space>
        )}
      </QueryBoundary>
    </>
  );
}
