import { FileExcelOutlined, FilePdfOutlined } from '@ant-design/icons';
import { Button, Card, Form, Select, Space } from 'antd';
import { useState } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { notify } from '@/lib/notify';
import { downloadReport } from '../api/reportApi';
import { reportLabels, reportTypeLabels } from '../labels';
import type { ReportFormat, ReportType } from '../types';

export function ReportBuilder() {
  const [type, setType] = useState<ReportType>('WEEKLY_UNIT_SUMMARY');
  const [busy, setBusy] = useState<ReportFormat | null>(null);

  const run = async (format: ReportFormat) => {
    setBusy(format);
    try {
      await downloadReport({ type, format });
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
