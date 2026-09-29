import type { LocationLevel, Role } from '@/features/auth';
import type { Location } from '../types';

export type LocationOption = { value: number; label: string };
export type LocationOptionGroup = { label: string; options: LocationOption[] };
/** antd `Select` bir ro'yxatda oddiy va guruhlangan variantlarni qabul qiladi. */
export type LocationSelectOptions = Array<LocationOption | LocationOptionGroup>;

/** Rolning hududiy darajasi; vazirlik rollari (`null`) hududga biriktirilmaydi. */
const ROLE_LOCATION_LEVEL: Record<Role, LocationLevel | null> = {
  MEGA_SUPER_ADMIN: null,
  SUPER_ADMIN: null,
  ADMIN: 'DISTRICT',
  USER: 'UNIT',
};

export function locationLevelForRole(role: Role | undefined): LocationLevel | null {
  return role ? ROLE_LOCATION_LEVEL[role] : null;
}

const byLabel = (a: LocationOption, b: LocationOption) => a.label.localeCompare(b.label);

const toOption = (location: Location): LocationOption => ({ value: location.id, label: location.name });

/**
 * Tanlanadigan hududlar: okrug darajasi — tekis ro'yxat; qism darajasi — ota okrug nomi bo'yicha guruhlangan
 * (bir xil nomli qismlarni farqlash va qidirishni osonlashtirish uchun).
 */
export function buildLocationOptions(
  locations: Location[],
  level: LocationLevel,
): LocationSelectOptions {
  const matching = locations.filter((location) => location.level === level);
  if (level !== 'UNIT') return matching.map(toOption).sort(byLabel);

  const parentNames = new Map(locations.map((location) => [location.id, location.name]));
  const groups = new Map<string, LocationOption[]>();
  for (const unit of matching) {
    const groupLabel = (unit.parentId !== null && parentNames.get(unit.parentId)) || '—';
    groups.set(groupLabel, [...(groups.get(groupLabel) ?? []), toOption(unit)]);
  }
  return [...groups.entries()]
    .map(([label, options]) => ({ label, options: options.sort(byLabel) }))
    .sort((a, b) => a.label.localeCompare(b.label));
}
