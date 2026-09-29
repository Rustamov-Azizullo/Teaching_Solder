import { shorten, sumValues, takeTop } from './chartData';

const items = [
  { id: 'a', name: 'A', value: 5 },
  { id: 'b', name: 'B', value: 9 },
  { id: 'c', name: 'C', value: 1 },
  { id: 'd', name: 'D', value: 3 },
];

describe('takeTop', () => {
  it('keeps everything when within the limit, sorted by value', () => {
    expect(takeTop(items, 4, 'Boshqalar').map((item) => item.name)).toEqual(['B', 'A', 'D', 'C']);
  });

  it('merges the tail into a single "other" item', () => {
    const result = takeTop(items, 3, 'Boshqalar');
    expect(result.map((item) => item.name)).toEqual(['B', 'A', 'Boshqalar']);
    expect(result[2].value).toBe(4);
    expect(sumValues(result)).toBe(sumValues(items));
  });
});

describe('shorten', () => {
  it('truncates long text with an ellipsis and leaves short text alone', () => {
    expect(shorten('abcdefghij', 5)).toBe('abcd…');
    expect(shorten('abc', 5)).toBe('abc');
  });
});
