import type { PermissionKey } from '@/features/auth';
import { useToggleDraft } from '@/hooks/useToggleDraft';
import type { UserPermissionChange, UserPermissionState } from '../types';
import { useUpdateUserPermissions, useUserPermissions } from './useAdmin';

export type UserPermissionRow = UserPermissionState & { isChecked: boolean; isLocked: boolean };

export type UserPermissionsEditor = {
  rows: UserPermissionRow[] | undefined;
  isLoading: boolean;
  error: unknown;
  refetch: () => void;
  isDirty: boolean;
  isSaving: boolean;
  toggle: (permission: PermissionKey, granted: boolean) => void;
  save: () => Promise<void>;
};

/**
 * Foydalanuvchiga shaxsiy (qo'shimcha) ruxsatlarni tahrirlash. Rol orqali berilgan ruxsat belgilangan va
 * qulflangan ko'rinadi; agar unga ortiqcha shaxsiy yozuv ham bo'lsa, uni olib tashlash uchun ochiq qoladi.
 */
export function useUserPermissionsEditor(userId: number): UserPermissionsEditor {
  const { data, isLoading, error, refetch } = useUserPermissions(userId);
  const { mutateAsync, isPending } = useUpdateUserPermissions(userId);
  const draft = useToggleDraft<PermissionKey>();

  const rows = data?.map((state) => {
    const granted = draft.valueOf(state.permission, state.granted);
    return { ...state, granted, isChecked: granted || state.grantedByRole, isLocked: state.grantedByRole && !granted };
  });

  const toggle = (permission: PermissionKey, granted: boolean) => {
    const savedGranted = data?.find((state) => state.permission === permission)?.granted ?? false;
    draft.set(permission, granted, savedGranted);
  };

  const save = async () => {
    const changes: UserPermissionChange[] = [...draft.changes].map(([permission, granted]) => ({ permission, granted }));
    await mutateAsync(changes);
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
    save,
  };
}
