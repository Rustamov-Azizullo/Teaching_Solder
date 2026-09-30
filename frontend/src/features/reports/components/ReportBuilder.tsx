import { FileExcelOutlined, FilePdfOutlined } from '@ant-design/icons';
import { Button, Card, Form, Select, Space } from 'antd';
import { useState } from 'react';
import { useAuth } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { notify } from '@/lib/notify';
import { downloadReport } from '../api/reportApi';
import { reportLabels, reportTypeLabels } from '../labels';
import type { ReportFormat, ReportType, WeeklySelection } from '../types';
import { defaultWeeklySelection, isWeeklySelectionComplete, toWeeklyParams, WeeklyViewFields } from './WeeklyViewFields';

export function ReportBuilder() {
  const { user } = useAuth();
  const [type, setType] = useState<ReportType>('WEEKLY_UNIT_SUMMARY');
  const [weekly, setWeekly] = useState<WeeklySelection>(() => defaultWeeklySelection(user?.role ?? 'USER'));
  const [busy, setBusy] = useState<ReportFormat | null>(null);
  const isWeekly = type === 'WEEKLY_UNIT_SUMMARY';
  const isTargetMissing = isWeekly && !isWeeklySelectionComplete(weekly);

  const run = async (format: ReportFormat) => {
    setBusy(format);
    try {
      await downloadReport({ type, format, ...(isWeekly ? toWeeklyParams(weekly) : {}) });
      notify.success(reportLabels.ready);
    } catch (error) {
      notify.error(await readableError(error));
    } finally {
      setBusy(null);
    }
  };

  return (
    <Card style={{ maxWidth: 640 }}>
      <Form layout="vertical">
        <Form.Item label={reportLabels.type}>
          <Select value={type} onChange={setType} options={Object.entries(reportTypeLabels).map(([value, label]) => ({ value, label }))} />
        </Form.Item>
        {isWeekly && user && (
          <WeeklyViewFields role={user.role} unitId={user.militaryUnitId ?? undefined} value={weekly} onChange={setWeekly} />
        )}
        <Space wrap>
          <Button type="primary" icon={<FileExcelOutlined />} loading={busy === 'XLSX'} disabled={isTargetMissing} onClick={() => run('XLSX')}>{reportLabels.xlsx}</Button>
          <Button icon={<FilePdfOutlined />} loading={busy === 'PDF'} disabled={isTargetMissing} onClick={() => run('PDF')}>{reportLabels.pdf}</Button>
        </Space>
      </Form>
    </Card>
  );
}

/** Blob javobdagi xato matnini o'qiydi (server JSON xabarini blob sifatida qaytaradi). */
async function readableError(error: unknown): Promise<string> {
  const data = (error as { response?: { data?: unknown } }).response?.data;
  if (data instanceof Blob) {
    try {
      return (JSON.parse(await data.text()) as { message?: string }).message ?? getErrorMessage(error);
    } catch {
      return getErrorMessage(error);
    }
  }
  return getErrorMessage(error);
}
