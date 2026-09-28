import type { PermissionKey } from '@/features/auth';
import { useToggleDraft } from '@/hooks/useToggleDraft';
import type { ConfigurableRole, MatrixCellKey, RolePermissionRow } from '../types';
import { buildMatrixRows, collectChanges, matrixCellKey } from '../utils/rolePermissionMatrix';
import { useRolePermissions, useUpdateRolePermissions } from './useRolePermissions';

export type RolePermissionsEditor = {
  rows: RolePermissionRow[] | undefined;
  isLoading: boolean;
  error: unknown;
  refetch: () => void;
  isDirty: boolean;
  isSaving: boolean;
  toggle: (role: ConfigurableRole, permission: PermissionKey, granted: boolean) => void;
  discard: () => void;
  save: () => Promise<void>;
};

/** Rol-ruxsat matritsasini ko'rsatish va saqlanmagan o'zgarishlarni boshqarish. */
export function useRolePermissionsEditor(): RolePermissionsEditor {
  const { data, isLoading, error, refetch } = useRolePermissions();
  const { mutateAsync, isPending } = useUpdateRolePermissions();
  const draft = useToggleDraft<MatrixCellKey>();

  const rows = data
    ? buildMatrixRows(data).map((row) => ({
        permission: row.permission,
        granted: {
          ADMIN: draft.valueOf(matrixCellKey('ADMIN', row.permission), row.granted.ADMIN),
          USER: draft.valueOf(matrixCellKey('USER', row.permission), row.granted.USER),
        },
      }))
    : undefined;

  const toggle = (role: ConfigurableRole, permission: PermissionKey, granted: boolean) => {
    const savedGranted = data?.find((entry) => entry.role === role && entry.permission === permission)?.granted ?? false;
    draft.set(matrixCellKey(role, permission), granted, savedGranted);
  };

  const save = async () => {
    await mutateAsync(collectChanges(data ?? [], draft.changes));
    draft.reset();
  };

  return {
    rows,
    isLoading,
    error,
    refetch: () => void refetch(),
    isDirty: draft.isDirty,
    isSaving: isPending,
    toggle,
    discard: draft.reset,
    save,
  };
}
