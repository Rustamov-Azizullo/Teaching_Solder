import { PASSPORT_PATTERN, PHONE_PATTERN, PINFL_PATTERN } from './validators';

describe('PINFL_PATTERN', () => {
  it.each([
    ['12345678901234', true],
    ['1234567890123', false],
    ['123456789012345', false],
    ['1234567890123a', false],
    ['', false],
  ])('%j -> %s', (value, expected) => {
    expect(PINFL_PATTERN.test(value)).toBe(expected);
  });
});

describe('PASSPORT_PATTERN', () => {
  it.each([
    ['AA1234567', true],
    ['aa1234567', false],
    ['A11234567', false],
    ['AA123456', false],
    ['AA12345678', false],
    ['', false],
  ])('%j -> %s', (value, expected) => {
    expect(PASSPORT_PATTERN.test(value)).toBe(expected);
  });
});

describe('PHONE_PATTERN', () => {
  it.each([
    ['+998901234567', true],
    ['998901234567', false],
    ['+99890123456', false],
    ['+9989012345678', false],
    ['+998 90 123 45 67', false],
    ['', false],
  ])('%j -> %s', (value, expected) => {
    expect(PHONE_PATTERN.test(value)).toBe(expected);
  });
});
