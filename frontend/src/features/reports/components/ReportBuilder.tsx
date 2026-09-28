import { FileExcelOutlined, FilePdfOutlined } from '@ant-design/icons';
import { Button, Card, Form, Select, Space } from 'antd';
import { useState } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { API_DATE_FORMAT, dayjs } from '@/lib/dayjs';
import { notify } from '@/lib/notify';
import { downloadReport } from '../api/reportApi';
import { reportLabels, reportTypeLabels } from '../labels';
import type { ReportFormat, ReportGroupType, ReportType } from '../types';

const DEFAULT_RANGE_DAYS = 7;
const DEFAULT_GROUP_TYPE: ReportGroupType = 'VOCATIONAL';

export function ReportBuilder() {
  const [type, setType] = useState<ReportType>('COURSE_COMPLETION');
  const [groupType] = useState<ReportGroupType>(DEFAULT_GROUP_TYPE);
  const [busy, setBusy] = useState<ReportFormat | null>(null);

  const run = async (format: ReportFormat) => {
    setBusy(format);
    try {
      const range = [dayjs().subtract(DEFAULT_RANGE_DAYS, 'day'), dayjs()];
      await downloadReport({ type, format, groupType, from: range[0].format(API_DATE_FORMAT), to: range[1].format(API_DATE_FORMAT) });
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
        <Space wrap>
          <Button type="primary" icon={<FileExcelOutlined />} loading={busy === 'XLSX'} onClick={() => run('XLSX')}>{reportLabels.xlsx}</Button>
          <Button icon={<FilePdfOutlined />} loading={busy === 'PDF'} onClick={() => run('PDF')}>{reportLabels.pdf}</Button>
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
