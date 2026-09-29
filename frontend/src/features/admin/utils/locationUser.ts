import type { LocationLevel, Role } from '@/features/auth';
import type { CreateUserRequest, RoleOption } from '../types';

export type LocationUserValues = { username: string; password: string; fullName: string; role?: Role };

/** Hudud darajasiga mos rollar (okrug -> Admin, qism -> User, vazirlik -> SuperAdmin/Mega SuperAdmin). */
export function rolesForLevel(roles: RoleOption[], level: LocationLevel): RoleOption[] {
  return roles.filter((option) => option.scopeLevel === level);
}

/** Vazirlik rollari hududga biriktirilmaydi, shuning uchun `locationId` faqat okrug/qism uchun yuboriladi. */
export function toCreateUserRequest(
  values: LocationUserValues,
  level: LocationLevel,
  locationId: number,
  roles: RoleOption[],
): CreateUserRequest | null {
  const role = values.role ?? rolesForLevel(roles, level)[0]?.code;
  if (!role) return null;
  return {
    username: values.username,
    password: values.password,
    fullName: values.fullName,
    role,
    locationId: level === 'REPUBLIC' ? undefined : locationId,
  };
}
