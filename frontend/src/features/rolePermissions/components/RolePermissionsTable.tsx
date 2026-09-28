import { Checkbox, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { permissionLabels, roleLabels, type PermissionKey } from '@/features/auth';
import { rolePermissionsLabels } from '../labels';
import type { ConfigurableRole, RolePermissionRow } from '../types';
import { CONFIGURABLE_ROLES } from '../utils/rolePermissionMatrix';

type RolePermissionsTableProps = {
  rows: RolePermissionRow[];
  onToggle: (role: ConfigurableRole, permission: PermissionKey, granted: boolean) => void;
};

export function RolePermissionsTable({ rows, onToggle }: RolePermissionsTableProps) {
  const columns: ColumnsType<RolePermissionRow> = [
    { title: rolePermissionsLabels.permission, dataIndex: 'permission', render: (permission: PermissionKey) => permissionLabels[permission] },
    ...CONFIGURABLE_ROLES.map((role) => ({
      title: roleLabels[role],
      key: role,
      align: 'center' as const,
      width: 120,
      render: (_: unknown, row: RolePermissionRow) => (
        <Checkbox
          checked={row.granted[role]}
          onChange={(event) => onToggle(role, row.permission, event.target.checked)}
          aria-label={`${roleLabels[role]}: ${permissionLabels[row.permission]}`}
        />
      ),
    })),
  ];

  return (
    <Table<RolePermissionRow>
      rowKey="permission"
      dataSource={rows}
      columns={columns}
      pagination={false}
      size="small"
      scroll={{ x: 'max-content' }}
    />
  );
}
