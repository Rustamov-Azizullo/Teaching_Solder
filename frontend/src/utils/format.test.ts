import { formatDate, formatDateTime, formatPercent, formatTime } from './format';

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
