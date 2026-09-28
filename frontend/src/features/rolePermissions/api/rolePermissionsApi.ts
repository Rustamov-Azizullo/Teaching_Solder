import { apiClient } from '@/lib/apiClient';
import type { RolePermissionEntry } from '../types';

export const rolePermissionsApi = {
  matrix: () => apiClient.get<RolePermissionEntry[]>('/role-permissions').then((r) => r.data),
  /** Faqat yuborilgan juftliklar o'zgaradi; javobda yangilangan to'liq matritsa qaytadi. */
  update: (changes: RolePermissionEntry[]) =>
    apiClient.put<RolePermissionEntry[]>('/role-permissions', changes).then((r) => r.data),
};
