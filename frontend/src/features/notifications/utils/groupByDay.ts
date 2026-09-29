import type { AppNotification } from '../types';

export type DayGroupKey = 'today' | 'yesterday' | 'earlier';
export type DayGroup = { key: DayGroupKey; items: AppNotification[] };

const GROUP_ORDER: readonly DayGroupKey[] = ['today', 'yesterday', 'earlier'];

const startOfDay = (date: Date): number => new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime();

/** Bildirishnomalarni "Bugun / Kecha / Oldinroq" bo'yicha guruhlaydi; bo'sh guruhlar tushib qoladi. */
export function groupByDay(items: AppNotification[], now: Date = new Date()): DayGroup[] {
  const today = startOfDay(now);
  const msPerDay = 24 * 60 * 60 * 1000;
  const buckets: Record<DayGroupKey, AppNotification[]> = { today: [], yesterday: [], earlier: [] };
  for (const item of items) {
    const day = startOfDay(new Date(item.createdAt));
    if (day >= today) buckets.today.push(item);
    else if (day >= today - msPerDay) buckets.yesterday.push(item);
    else buckets.earlier.push(item);
  }
  return GROUP_ORDER.filter((key) => buckets[key].length > 0).map((key) => ({ key, items: buckets[key] }));
}
