import { Segmented, Select, Typography } from 'antd';
import { absenceReasonLabels, attendanceLabels, statusLabels } from '../labels';
import type { AbsenceReason, AttendanceStatus, DraftEntry } from '../types';

const TOUCH_TARGET_PX = 44;

const STATUS_OPTIONS = [
  { value: 'PRESENT' as const, label: statusLabels.PRESENT },
  { value: 'ABSENT' as const, label: statusLabels.ABSENT },
];

const REASON_OPTIONS = Object.entries(absenceReasonLabels).map(([value, label]) => ({
  value: value as AbsenceReason,
  label,
}));

type RosterRowProps = {
  index: number;
  soldierId: number;
  fullName: string;
  entry: DraftEntry | undefined;
  disabled: boolean;
  onStatusChange: (status: AttendanceStatus) => void;
  onReasonChange: (reason: AbsenceReason) => void;
};

/** Bitta askar qatori: telefonda bir bosishda "keldi/kelmadi", kelmasa — sabab tanlanadi. */
export function RosterRow({ index, soldierId, fullName, entry, disabled, onStatusChange, onReasonChange }: RosterRowProps) {
  const status = entry?.status ?? 'PRESENT';
  const needsReason = status === 'ABSENT' && !entry?.reason;
  const nameId = `roster-name-${soldierId}`;

  return (
    <li
      id={`roster-row-${soldierId}`}
      aria-labelledby={nameId}
      style={{ listStyle: 'none', padding: '10px 0', borderBottom: '1px solid rgba(128,128,128,.2)' }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
        <span id={nameId} style={{ fontWeight: 500 }}>
          <span aria-hidden="true">{index + 1}. </span>
          {fullName}
        </span>
        <Segmented<AttendanceStatus>
          size="large"
          disabled={disabled}
          value={status}
          onChange={onStatusChange}
          options={STATUS_OPTIONS}
          aria-label={`${fullName}: ${statusLabels[status]}`}
          style={{ minHeight: TOUCH_TARGET_PX }}
        />
      </div>
      {status === 'ABSENT' && (
        <>
          <Select<AbsenceReason>
            disabled={disabled}
            size="large"
            status={needsReason ? 'error' : undefined}
            placeholder={attendanceLabels.reasonPlaceholder}
            aria-label={`${attendanceLabels.reasonPlaceholder}: ${fullName}`}
            value={entry?.reason}
            onChange={onReasonChange}
            style={{ width: '100%', marginTop: 8 }}
            options={REASON_OPTIONS}
          />
          {needsReason && (
            <Typography.Text type="danger" role="alert">
              {attendanceLabels.reasonMissing}
            </Typography.Text>
          )}
        </>
      )}
    </li>
  );
}
