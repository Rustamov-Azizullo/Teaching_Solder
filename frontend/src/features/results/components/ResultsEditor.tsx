import { CheckCircleOutlined } from '@ant-design/icons';
import { Alert, Button, DatePicker, Input, InputNumber, Popconfirm, Select, Space, Table } from 'antd';
import { useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { AttachmentPanel } from '@/features/attachments';
import { useCan } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { API_DATE_FORMAT, DISPLAY_DATE_FORMAT, dayjs } from '@/lib/dayjs';
import { notify } from '@/lib/notify';
import { useApproveResults, useResultsSheet, useSaveResults } from '../hooks/useResults';
import { courseStatusLabels, resultLabels } from '../labels';
import type { CourseStatus, ResultInput, ResultRow, ResultsSheet } from '../types';

type Draft = Record<number, ResultInput>;

const toDraft = (rows: ResultRow[]): Draft =>
  Object.fromEntries(rows.map((r) => [r.soldierId, {
    soldierId: r.soldierId, status: r.status ?? 'STUDIED', examGrade: r.examGrade, dropReason: r.dropReason ?? undefined,
    certificateNo: r.certificateNo ?? undefined, certificateDate: r.certificateDate ?? undefined, certificateIssuer: r.certificateIssuer ?? undefined,
  }]));

function SheetBody({ sheet }: { sheet: ResultsSheet }) {
  const canWrite = useCan('resultWrite');
  const canApprove = useCan('leaderAssign');
  const save = useSaveResults(sheet.groupId);
  const approve = useApproveResults(sheet.groupId);
  const [draft, setDraft] = useState<Draft>(() => toDraft(sheet.rows));
  const readOnly = !canWrite || sheet.approved;
  const c = resultLabels.columns;

  const patch = (id: number, changes: Partial<ResultInput>) => setDraft((cur) => ({ ...cur, [id]: { ...cur[id], ...changes } }));

  const run = async (action: () => Promise<unknown>, message: string) => {
    try { await action(); notify.success(message); } catch (error) { notify.error(getErrorMessage(error)); }
  };

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      {sheet.approved && <Alert type="success" showIcon message={resultLabels.approved(sheet.approvedBy)} />}
      <Table<ResultRow> rowKey="soldierId" size="small" pagination={false} dataSource={sheet.rows} scroll={{ x: 'max-content' }}
        columns={[
          { title: c.name, dataIndex: 'fullName' },
          { title: c.status, render: (_: unknown, r) => (
            <Select<CourseStatus> size="small" style={{ width: 170 }} disabled={readOnly} value={draft[r.soldierId]?.status}
              onChange={(status) => patch(r.soldierId, { status })}
              options={Object.entries(courseStatusLabels).map(([value, label]) => ({ value: value as CourseStatus, label }))} />) },
          { title: c.grade, render: (_: unknown, r) => (
            <InputNumber size="small" min={0} max={100} disabled={readOnly} value={draft[r.soldierId]?.examGrade}
              onChange={(v) => patch(r.soldierId, { examGrade: v })} />) },
          { title: c.reason, render: (_: unknown, r) => draft[r.soldierId]?.status === 'DROPPED' && (
            <Input size="small" disabled={readOnly} value={draft[r.soldierId]?.dropReason} onChange={(e) => patch(r.soldierId, { dropReason: e.target.value })} />) },
          { title: c.certNo, render: (_: unknown, r) => draft[r.soldierId]?.status === 'CERTIFIED' && (
            <Input size="small" disabled={readOnly} value={draft[r.soldierId]?.certificateNo} onChange={(e) => patch(r.soldierId, { certificateNo: e.target.value })} />) },
          { title: c.certDate, render: (_: unknown, r) => draft[r.soldierId]?.status === 'CERTIFIED' && (
            <DatePicker size="small" disabled={readOnly} format={DISPLAY_DATE_FORMAT}
              value={draft[r.soldierId]?.certificateDate ? dayjs(draft[r.soldierId].certificateDate) : null}
              onChange={(d) => patch(r.soldierId, { certificateDate: d?.format(API_DATE_FORMAT) })} />) },
          { title: c.issuer, render: (_: unknown, r) => draft[r.soldierId]?.status === 'CERTIFIED' && (
            <Input size="small" disabled={readOnly} value={draft[r.soldierId]?.certificateIssuer} onChange={(e) => patch(r.soldierId, { certificateIssuer: e.target.value })} />) },
        ]}
      />
      {!readOnly && (
        <Space wrap>
          <Button type="primary" loading={save.isPending} onClick={() => run(() => save.mutateAsync(Object.values(draft)), resultLabels.saved)}>{resultLabels.save}</Button>
          {canApprove && (
            <Popconfirm title={resultLabels.confirmApprove} onConfirm={() => run(() => approve.mutateAsync(), resultLabels.approvedDone)}>
              <Button icon={<CheckCircleOutlined />} loading={approve.isPending}>{resultLabels.approve}</Button>
            </Popconfirm>
          )}
        </Space>
      )}
      <AttachmentPanel ownerType="RESULT" ownerId={sheet.groupId} title={resultLabels.minutes} />
    </Space>
  );
}

export function ResultsEditor({ groupId }: { groupId: number }) {
  const { data, isLoading, error, refetch } = useResultsSheet(groupId);
  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {(sheet) => <SheetBody key={`${sheet.groupId}-${sheet.approved}-${sheet.rows.length}`} sheet={sheet} />}
    </QueryBoundary>
  );
}
