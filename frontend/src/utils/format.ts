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
