import type { AppNotification } from '../types';
import { groupByDay } from './groupByDay';

const make = (id: number, createdAt: string): AppNotification => ({ id, title: 't', body: 'b', link: null, read: false, createdAt });

describe('groupByDay', () => {
  const now = new Date(2026, 8, 28, 15, 0, 0);

  it('splits notifications into today, yesterday and earlier, dropping empty groups', () => {
    const groups = groupByDay(
      [make(1, new Date(2026, 8, 28, 9, 0).toISOString()), make(2, new Date(2026, 8, 27, 20, 0).toISOString()), make(3, new Date(2026, 8, 1).toISOString())],
      now,
    );
    expect(groups.map((group) => [group.key, group.items.map((item) => item.id)])).toEqual([
      ['today', [1]], ['yesterday', [2]], ['earlier', [3]],
    ]);
  });

  it('returns nothing for an empty list', () => {
    expect(groupByDay([], now)).toEqual([]);
  });
});
