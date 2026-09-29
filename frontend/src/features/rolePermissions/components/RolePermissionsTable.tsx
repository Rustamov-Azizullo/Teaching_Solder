import { LockOutlined } from '@ant-design/icons';
import { Checkbox, Table, Tooltip, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { permissionLabels, roleLabels, useAuth, type PermissionKey } from '@/features/auth';
import { rolePermissionsLabels } from '../labels';
import type { ConfigurableRole, RolePermissionRow } from '../types';
import { PERMISSION_GROUPS } from '../utils/permissionGroups';
import { PERMISSION_PAGES } from '../utils/permissionPages';
import { CONFIGURABLE_ROLES, canEditRole } from '../utils/rolePermissionMatrix';

const ROLE_COLUMN_WIDTH = 130;

type RolePermissionsTableProps = {
  /** Ko'rsatiladigan (qidiruv bilan filtrlangan) qatorlar. */
  rows: RolePermissionRow[];
  /** Barcha qatorlar: ustun sarlavhasidagi hisob va "hammasini belgilash" shularga tegishli. */
  allRows: RolePermissionRow[];
  onToggle: (role: ConfigurableRole, permission: PermissionKey, granted: boolean) => void;
  onToggleAll: (role: ConfigurableRole, granted: boolean) => void;
};

/** Bo'lim nomi faqat bo'lim birinchi qatorida chiqadi, qolgan qatorlar bilan birlashtiriladi. */
function groupRowSpan(rows: RolePermissionRow[], index: number): number {
  const group = PERMISSION_GROUPS[rows[index].permission];
  if (index > 0 && PERMISSION_GROUPS[rows[index - 1].permission] === group) return 0;
  let span = 1;
  while (index + span < rows.length && PERMISSION_GROUPS[rows[index + span].permission] === group) span += 1;
  return span;
}

export function RolePermissionsTable({ rows, allRows, onToggle, onToggleAll }: RolePermissionsTableProps) {
  const { user } = useAuth();

  const roleHeader = (role: ConfigurableRole) => {
    const isEditable = canEditRole(user?.role, role);
    const grantedCount = allRows.filter((row) => row.granted[role]).length;
    return (
      <Tooltip title={rolePermissionsLabels.granted(grantedCount, allRows.length)}>
        <Checkbox
          checked={grantedCount === allRows.length}
          indeterminate={grantedCount > 0 && grantedCount < allRows.length}
          disabled={!isEditable}
          onChange={(event) => onToggleAll(role, event.target.checked)}
          aria-label={rolePermissionsLabels.selectAll(roleLabels[role])}
        >
          {roleLabels[role]}
          {!isEditable && <LockOutlined style={{ marginInlineStart: 4 }} aria-label={rolePermissionsLabels.lockedRoleHint} />}
        </Checkbox>
      </Tooltip>
    );
  };

  const columns: ColumnsType<RolePermissionRow> = [
    {
      title: rolePermissionsLabels.group,
      key: 'group',
      width: 190,
      onCell: (_, index) => ({ rowSpan: groupRowSpan(rows, index ?? 0) }),
      render: (_: unknown, row) => rolePermissionsLabels.groups[PERMISSION_GROUPS[row.permission]],
    },
    { title: rolePermissionsLabels.permission, dataIndex: 'permission', render: (permission: PermissionKey) => permissionLabels[permission] },
    {
      title: rolePermissionsLabels.pages,
      key: 'pages',
      responsive: ['lg'],
      render: (_: unknown, row) => <Typography.Text type="secondary">{PERMISSION_PAGES[row.permission]}</Typography.Text>,
    },
    ...CONFIGURABLE_ROLES.map((role) => ({
      title: roleHeader(role),
      key: role,
      align: 'center' as const,
      width: ROLE_COLUMN_WIDTH,
      render: (_: unknown, row: RolePermissionRow) => (
        <Checkbox
          checked={row.granted[role]}
          disabled={!canEditRole(user?.role, role)}
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
      bordered
      sticky
      scroll={{ x: 'max-content' }}
      locale={{ emptyText: rolePermissionsLabels.noMatches }}
    />
  );
}
