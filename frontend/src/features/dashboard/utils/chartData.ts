/** Diagramma uchun umumiy element: `id` bosilganda tanlash uchun, `name` — yorliq, `value` — qiymat. */
export type NamedValue = { id: number | string; name: string; value: number };

export const sumValues = (items: readonly { value: number }[]): number => items.reduce((sum, item) => sum + item.value, 0);

/** Eng katta `limit` ta elementni qoldirib, qolganlarini bitta "Boshqalar" elementiga yig'adi (ixcham diagramma uchun). */
export function takeTop(items: NamedValue[], limit: number, otherLabel: string): NamedValue[] {
  const sorted = [...items].sort((a, b) => b.value - a.value);
  if (sorted.length <= limit) return sorted;
  const head = sorted.slice(0, limit - 1);
  const rest = sorted.slice(limit - 1);
  return [...head, { id: 'other', name: otherLabel, value: sumValues(rest) }];
}

/** Uzun yorliqni o'qib bo'ladigan qisqa ko'rinishga keltiradi. */
export function shorten(text: string, maxLength: number): string {
  return text.length <= maxLength ? text : `${text.slice(0, maxLength - 1)}…`;
}
