import { Alert, Button, Card, Input, Space, Switch, Typography } from 'antd';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageHeader, QueryBoundary } from '@/components/ui';
import { useCan } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { formatDate, formatTime } from '@/utils/format';
import { useAttendanceDraft } from '../hooks/useAttendanceDraft';
import { useAttendanceSheet, useRecordAttendance } from '../hooks/useAttendance';
import { attendanceLabels } from '../labels';
import type { AttendanceSheet } from '../types';
import { RosterRow } from './RosterRow';
import { notify } from '@/lib/notify';

const lessonRosterLabel = (groupName: string) => `${groupName}: ${attendanceLabels.title}`;

/** `key` (mashg'ulot id + davomat kiritilganligi) forma holatini saqlangan ma'lumot kelganda yangilaydi. */
function SheetBody({ sheet, lessonId }: { sheet: AttendanceSheet; lessonId: number }) {
  const navigate = useNavigate();
  const canWrite = useCan('attendanceWrite');
  const [teacherPresent, setTeacherPresent] = useState(sheet.lesson.teacherPresent ?? true);
  const [topic, setTopic] = useState(sheet.lesson.topic ?? '');
  const { draft, entries, presentCount, firstMissingReasonId, setStatus, setReason, markAllPresent } = useAttendanceDraft(sheet.roster);
  const { mutateAsync, isPending } = useRecordAttendance(lessonId);
  const isReadOnly = !canWrite || !sheet.editable;

  const handleSave = async () => {
    if (firstMissingReasonId !== undefined) {
      notify.warning(attendanceLabels.reasonRequired);
      document.getElementById(`roster-row-${firstMissingReasonId}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      return;
    }
    try {
      await mutateAsync({ teacherPresent, topic: topic || undefined, entries });
      notify.success(attendanceLabels.saved);
      navigate('/attendance');
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  const { lesson } = sheet;
  return (
    <>
      <PageHeader
        title={lesson.groupName}
        subtitle={`${formatDate(lesson.lessonDate)} · ${formatTime(lesson.startTime)}–${formatTime(lesson.endTime)}`}
      />
      {isReadOnly && <Alert type="warning" showIcon message={sheet.readOnlyReason ?? attendanceLabels.readOnly} style={{ marginBottom: 12 }} />}
      <Card style={{ maxWidth: 680 }}>
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <Input placeholder={attendanceLabels.topic} value={topic} disabled={isReadOnly} onChange={(e) => setTopic(e.target.value)} />
          <Space>
            <Switch checked={teacherPresent} disabled={isReadOnly} onChange={setTeacherPresent} />
            <span>{attendanceLabels.teacherPresent}</span>
          </Space>
          <Space style={{ justifyContent: 'space-between', width: '100%' }} wrap>
            <Typography.Text strong>{attendanceLabels.summary(presentCount, entries.length)}</Typography.Text>
            {!isReadOnly && <Button onClick={markAllPresent}>{attendanceLabels.allPresent}</Button>}
          </Space>
          {sheet.roster.length === 0 && <Alert type="info" message={attendanceLabels.empty} />}
          <ul style={{ margin: 0, padding: 0 }} aria-label={lessonRosterLabel(sheet.lesson.groupName)}>
            {sheet.roster.map((row, index) => (
              <RosterRow
                key={row.soldierId}
                soldierId={row.soldierId}
                index={index}
                fullName={row.fullName}
                entry={draft[row.soldierId]}
                disabled={isReadOnly}
                onStatusChange={(status) => setStatus(row.soldierId, status)}
                onReasonChange={(reason) => setReason(row.soldierId, reason)}
              />
            ))}
          </ul>
          {!isReadOnly && (
            <Button type="primary" size="large" block loading={isPending} disabled={sheet.roster.length === 0} onClick={handleSave}>
              {attendanceLabels.enter}
            </Button>
          )}
        </Space>
      </Card>
    </>
  );
}

export function AttendanceSheetForm({ lessonId }: { lessonId: number }) {
  const { data, isLoading, error, refetch } = useAttendanceSheet(lessonId);
  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {(sheet) => <SheetBody key={sheet.lesson.id + String(sheet.lesson.attendanceRecorded)} sheet={sheet} lessonId={lessonId} />}
    </QueryBoundary>
  );
}
