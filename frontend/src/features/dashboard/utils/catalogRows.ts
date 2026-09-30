import type { CatalogEntry, CatalogUnit } from '../types';

/** Jadval qatori: nom bo'yicha jamlangan ko'rsatkichlar. */
export type CatalogRow = { name: string; groups: number; soldiers: number };

const byName = (a: CatalogRow, b: CatalogRow) => a.name.localeCompare(b.name);

/** Kasb yo'nalishlari: noyob nomlar (guruh va askar soni yo'nalishga bog'liq emas). */
export function toDirectionRows(units: CatalogUnit[]): CatalogRow[] {
  const rows = new Map<string, CatalogRow>();
  units.forEach((unit) => unit.directions.forEach((name) => rows.set(name, { name, groups: 0, soldiers: 0 })));
  return [...rows.values()].sort(byName);
}

/** Kasblar yoki fanlar: qismlar bo'yicha guruhlar va askarlar nom bo'yicha yig'iladi (askar faqat bitta qismda bo'ladi). */
export function toEntryRows(units: CatalogUnit[], pick: (unit: CatalogUnit) => CatalogEntry[]): CatalogRow[] {
  const rows = new Map<string, CatalogRow>();
  units.forEach((unit) => pick(unit).forEach((entry) => {
    const row = rows.get(entry.name) ?? { name: entry.name, groups: 0, soldiers: 0 };
    row.groups += entry.groups;
    row.soldiers += entry.soldiers;
    rows.set(entry.name, row);
  }));
  return [...rows.values()].sort((a, b) => b.soldiers - a.soldiers || byName(a, b));
}
