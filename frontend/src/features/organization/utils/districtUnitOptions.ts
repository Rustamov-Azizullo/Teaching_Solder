import type { LocationNode } from '../types';

export type SelectOption = { value: number; label: string };

/** Okrug filtridagi "Vazirlik" tanlovi: okrug bo'yicha cheklov yo'q (hududlar daraxtining ildizi). */
export const MINISTRY_OPTION_VALUE = 0;

const byLabel = (a: SelectOption, b: SelectOption) => a.label.localeCompare(b.label, 'uz');

/** Birinchi qator — vazirlik (daraxt ildizi, bo'lsa), keyin okruglar nomi bo'yicha. Qiymat — `locations` yozuvi id si. */
export function toDistrictOptions(locations: LocationNode[]): SelectOption[] {
  const republic = locations.find((l) => l.level === 'REPUBLIC');
  const districts = locations.filter((l) => l.level === 'DISTRICT').map((d) => ({ value: d.id, label: d.name })).sort(byLabel);
  return [...(republic ? [{ value: MINISTRY_OPTION_VALUE, label: republic.name }] : []), ...districts];
}

/** Tanlangan okrugdagi harbiy qismlar (vazirlik tanlansa — hammasi). Qiymat — `military_units` id si. */
export function toUnitOptions(locations: LocationNode[], districtLocationId: number): SelectOption[] {
  return locations
    .filter((l) => l.level === 'UNIT' && l.militaryUnitId !== null
      && (districtLocationId === MINISTRY_OPTION_VALUE || l.parentId === districtLocationId))
    .map((l) => ({ value: l.militaryUnitId as number, label: l.name }))
    .sort(byLabel);
}

/** Harbiy qism qaysi okrugga (locations id) tegishli; topilmasa `undefined`. */
export function districtLocationOfUnit(locations: LocationNode[], unitId: number | undefined): number | undefined {
  return locations.find((l) => l.level === 'UNIT' && l.militaryUnitId === unitId)?.parentId ?? undefined;
}

/** Okrugning `military_districts` id si (backend filtrlari shuni kutadi); vazirlik uchun `undefined`. */
export function militaryDistrictIdOf(locations: LocationNode[], districtLocationId: number): number | undefined {
  return locations.find((l) => l.id === districtLocationId)?.militaryDistrictId ?? undefined;
}

/** Yuqoridan pastga: vazirlik → okrug → harbiy qism nomlari. Qism topilmasa — bo'sh ro'yxat. */
export function unitAncestry(locations: LocationNode[], unitId: number | undefined): string[] {
  const unit = locations.find((l) => l.level === 'UNIT' && l.militaryUnitId === unitId);
  if (!unit) return [];
  const district = locations.find((l) => l.id === unit.parentId);
  const republic = locations.find((l) => l.id === district?.parentId);
  return [republic?.name, district?.name, unit.name].filter((name): name is string => Boolean(name));
}
