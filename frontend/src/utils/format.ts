import { dayjs, DISPLAY_DATE_FORMAT, DISPLAY_DATETIME_FORMAT } from '@/lib/dayjs';

export function formatDate(value: string | null | undefined): string {
  return value ? dayjs(value).format(DISPLAY_DATE_FORMAT) : '—';
}

export function formatDateTime(value: string | null | undefined): string {
  return value ? dayjs(value).format(DISPLAY_DATETIME_FORMAT) : '—';
}

export function formatPercent(value: number): string {
  return `${value.toFixed(1)}%`;
}

export function formatTime(value: string): string {
  return value.slice(0, 5);
}

const MINUTES_PER_HOUR = 60;
const HOURS_PER_DAY = 24;
const RELATIVE_DAYS_LIMIT = 7;

/** "hozirgina", "5 daqiqa oldin", "3 soat oldin", "2 kun oldin"; bir haftadan eskisi — oddiy sana. */
export function formatRelativeTime(value: string | null | undefined, now: Date = new Date()): string {
  if (!value) return '—';
  const minutes = Math.floor((now.getTime() - new Date(value).getTime()) / 60_000);
  if (minutes < 1) return 'hozirgina';
  if (minutes < MINUTES_PER_HOUR) return `${minutes} daqiqa oldin`;
  const hours = Math.floor(minutes / MINUTES_PER_HOUR);
  if (hours < HOURS_PER_DAY) return `${hours} soat oldin`;
  const days = Math.floor(hours / HOURS_PER_DAY);
  if (days < RELATIVE_DAYS_LIMIT) return `${days} kun oldin`;
  return formatDate(value);
}
