import type { Role } from './types';

/** Backenddagi `Role.label()` bilan bir xil. */
export const roleLabels: Record<Role, string> = {
  MEGA_SUPER_ADMIN: 'Mega SuperAdmin',
  SUPER_ADMIN: 'SuperAdmin',
  ADMIN: 'Admin',
  USER: 'User',
};
