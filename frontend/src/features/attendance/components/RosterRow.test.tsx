import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { attendanceLabels, statusLabels } from '../labels';
import type { DraftEntry } from '../types';
import { RosterRow } from './RosterRow';

const renderRow = (entry: DraftEntry | undefined, overrides: Partial<Parameters<typeof RosterRow>[0]> = {}) => {
  const onStatusChange = vi.fn();
  const onReasonChange = vi.fn();
  render(
    <RosterRow
      index={0}
      soldierId={1}
      fullName="Ali Valiyev"
      entry={entry}
      disabled={false}
      onStatusChange={onStatusChange}
      onReasonChange={onReasonChange}
      {...overrides}
    />,
  );
  return { onStatusChange, onReasonChange };
};

describe('RosterRow', () => {
  it('exposes the soldier name as the accessible name of the row', () => {
    renderRow({ status: 'PRESENT' });
    expect(screen.getByRole('listitem', { name: 'Ali Valiyev' })).toBeInTheDocument();
    expect(screen.getByText('1.')).toBeInTheDocument();
  });

  it('does not show the reason select when present', () => {
    renderRow({ status: 'PRESENT' });
    expect(screen.queryByText(attendanceLabels.reasonPlaceholder)).not.toBeInTheDocument();
  });

  it('treats a missing entry as present', () => {
    renderRow(undefined);
    expect(screen.queryByText(attendanceLabels.reasonPlaceholder)).not.toBeInTheDocument();
  });

  it('shows the reason select when absent', () => {
    renderRow({ status: 'ABSENT' });
    expect(screen.getByText(attendanceLabels.reasonPlaceholder)).toBeInTheDocument();
  });

  it('calls onStatusChange with ABSENT when the absent option is clicked', async () => {
    const { onStatusChange } = renderRow({ status: 'PRESENT' });

    await userEvent.click(screen.getByText(statusLabels.ABSENT));

    expect(onStatusChange).toHaveBeenCalledWith('ABSENT');
  });

  it('calls onStatusChange with PRESENT when the present option is clicked', async () => {
    const { onStatusChange } = renderRow({ status: 'ABSENT' });

    await userEvent.click(screen.getByText(statusLabels.PRESENT));

    expect(onStatusChange).toHaveBeenCalledWith('PRESENT');
  });
});
