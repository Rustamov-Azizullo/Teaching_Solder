import { act, renderHook } from '@testing-library/react';
import type { RosterEntry } from '../types';
import { useAttendanceDraft } from './useAttendanceDraft';

const roster: RosterEntry[] = [
  { soldierId: 1, fullName: 'A', status: null, reason: null },
  { soldierId: 2, fullName: 'B', status: 'ABSENT', reason: 'ILLNESS' },
  { soldierId: 3, fullName: 'C', status: 'ABSENT', reason: null },
];

describe('useAttendanceDraft', () => {
  it('defaults to PRESENT when the roster status is null and keeps recorded values', () => {
    const { result } = renderHook(() => useAttendanceDraft(roster));

    expect(result.current.draft[1]).toEqual({ status: 'PRESENT', reason: undefined });
    expect(result.current.draft[2]).toEqual({ status: 'ABSENT', reason: 'ILLNESS' });
  });

  it('counts present soldiers and detects absentees without a reason', () => {
    const { result } = renderHook(() => useAttendanceDraft(roster));

    expect(result.current.presentCount).toBe(1);
    expect(result.current.hasMissingReason).toBe(true);
  });

  it('setStatus to ABSENT marks the soldier absent and requires a reason', () => {
    const { result } = renderHook(() => useAttendanceDraft([roster[0]]));

    act(() => result.current.setStatus(1, 'ABSENT'));

    expect(result.current.draft[1].status).toBe('ABSENT');
    expect(result.current.hasMissingReason).toBe(true);
    expect(result.current.presentCount).toBe(0);
  });

  it('setReason sets the reason and marks the soldier ABSENT', () => {
    const { result } = renderHook(() => useAttendanceDraft([roster[0]]));

    act(() => result.current.setReason(1, 'DUTY'));

    expect(result.current.draft[1]).toEqual({ status: 'ABSENT', reason: 'DUTY' });
    expect(result.current.hasMissingReason).toBe(false);
  });

  it('setStatus back to PRESENT clears the reason', () => {
    const { result } = renderHook(() => useAttendanceDraft([roster[1]]));

    act(() => result.current.setStatus(2, 'PRESENT'));

    expect(result.current.draft[2]).toEqual({ status: 'PRESENT', reason: undefined });
  });

  it('markAllPresent makes everyone present with no missing reasons', () => {
    const { result } = renderHook(() => useAttendanceDraft(roster));

    act(() => result.current.markAllPresent());

    expect(result.current.presentCount).toBe(3);
    expect(result.current.hasMissingReason).toBe(false);
  });

  it('exposes entries shaped for the API', () => {
    const { result } = renderHook(() => useAttendanceDraft([roster[1]]));

    expect(result.current.entries).toEqual([{ soldierId: 2, status: 'ABSENT', reason: 'ILLNESS' }]);
  });
});
