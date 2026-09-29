import { formatDate, formatDateTime, formatPercent, formatTime, formatRelativeTime } from './format';

describe('formatDate', () => {
  it('formats an ISO date as DD.MM.YYYY', () => {
    expect(formatDate('2024-03-05')).toBe('05.03.2024');
  });

  it.each([null, undefined, ''])('returns a dash for %j', (value) => {
    expect(formatDate(value)).toBe('—');
  });
});

describe('formatDateTime', () => {
  it('formats an ISO datetime as DD.MM.YYYY HH:mm', () => {
    expect(formatDateTime('2024-03-05T14:30:00')).toBe('05.03.2024 14:30');
  });

  it.each([null, undefined, ''])('returns a dash for %j', (value) => {
    expect(formatDateTime(value)).toBe('—');
  });
});

describe('formatPercent', () => {
  it.each([
    [0, '0.0%'],
    [12.345, '12.3%'],
    [100, '100.0%'],
  ])('formats %d as %s', (value, expected) => {
    expect(formatPercent(value)).toBe(expected);
  });
});

describe('formatTime', () => {
  it('keeps only HH:mm', () => {
    expect(formatTime('09:30:00')).toBe('09:30');
  });

  it('leaves an already short time unchanged', () => {
    expect(formatTime('09:30')).toBe('09:30');
  });
});

describe('formatRelativeTime', () => {
  const now = new Date('2026-09-28T12:00:00Z');

  it.each([
    ['2026-09-28T11:59:40Z', 'hozirgina'],
    ['2026-09-28T11:55:00Z', '5 daqiqa oldin'],
    ['2026-09-28T09:00:00Z', '3 soat oldin'],
    ['2026-09-26T12:00:00Z', '2 kun oldin'],
  ])('%s -> %s', (value, expected) => {
    expect(formatRelativeTime(value, now)).toBe(expected);
  });

  it('falls back to a plain date after a week and to a dash without a value', () => {
    expect(formatRelativeTime('2026-09-01T12:00:00Z', now)).toBe('01.09.2026');
    expect(formatRelativeTime(null, now)).toBe('—');
  });
});
