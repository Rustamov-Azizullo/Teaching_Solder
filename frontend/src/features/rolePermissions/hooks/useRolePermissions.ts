import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '@/lib/queryKeys';
import { rolePermissionsApi } from '../api/rolePermissionsApi';
import type { RolePermissionEntry } from '../types';

const ROLE_PERMISSIONS_KEY = ['role-permissions'] as const;

export const useRolePermissions = () => useQuery({ queryKey: ROLE_PERMISSIONS_KEY, queryFn: rolePermissionsApi.matrix });

export function useUpdateRolePermissions() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (changes: RolePermissionEntry[]) => rolePermissionsApi.update(changes),
    onSuccess: (matrix) => {
      queryClient.setQueryData(ROLE_PERMISSIONS_KEY, matrix);
      // Foydalanuvchilarning amaldagi ruxsatlari (va rol orqali berilganlar belgisi) o'zgaradi.
      return queryClient.invalidateQueries({ queryKey: queryKeys.users });
    },
  });
}
