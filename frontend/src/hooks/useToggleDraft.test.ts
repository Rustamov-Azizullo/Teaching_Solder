import { act, renderHook } from '@testing-library/react';
import { useToggleDraft } from './useToggleDraft';

describe('useToggleDraft', () => {
  it('falls back to the saved value when nothing was changed', () => {
    const { result } = renderHook(() => useToggleDraft<'A'>());

    expect(result.current.valueOf('A', true)).toBe(true);
    expect(result.current.isDirty).toBe(false);
  });

  it('records a change that differs from the saved value', () => {
    const { result } = renderHook(() => useToggleDraft<'A'>());

    act(() => result.current.set('A', false, true));

    expect(result.current.valueOf('A', true)).toBe(false);
    expect([...result.current.changes]).toEqual([['A', false]]);
  });

  it('drops the change once the value is toggled back to the saved value', () => {
    const { result } = renderHook(() => useToggleDraft<'A'>());

    act(() => result.current.set('A', true, false));
    act(() => result.current.set('A', false, false));

    expect(result.current.isDirty).toBe(false);
  });

  it('clears every change on reset', () => {
    const { result } = renderHook(() => useToggleDraft<'A' | 'B'>());

    act(() => {
      result.current.set('A', true, false);
      result.current.set('B', true, false);
    });
    act(() => result.current.reset());

    expect(result.current.changes.size).toBe(0);
  });
});
