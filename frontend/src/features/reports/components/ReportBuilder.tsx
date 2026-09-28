import { FileExcelOutlined, FilePdfOutlined } from '@ant-design/icons';
import { Button, Card, DatePicker, Form, Select, Space } from 'antd';
import type { Dayjs } from 'dayjs';
import { useState } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { API_DATE_FORMAT, DISPLAY_DATE_FORMAT, dayjs } from '@/lib/dayjs';
import { notify } from '@/lib/notify';
import { downloadReport } from '../api/reportApi';
import { reportGroupTypeLabels, reportLabels, reportTypeLabels } from '../labels';
import type { ReportFormat, ReportGroupType, ReportType } from '../types';

const DEFAULT_RANGE_DAYS = 7;
const isAttendance = (type: ReportType) => type === 'ATTENDANCE_DAILY' || type === 'ATTENDANCE_WEEKLY';

export function ReportBuilder() {
  const [type, setType] = useState<ReportType>('ATTENDANCE_DAILY');
  const [groupType, setGroupType] = useState<ReportGroupType>('VOCATIONAL');
  const [range, setRange] = useState<[Dayjs, Dayjs]>([dayjs().subtract(DEFAULT_RANGE_DAYS, 'day'), dayjs()]);
  const [busy, setBusy] = useState<ReportFormat | null>(null);

  const run = async (format: ReportFormat) => {
    setBusy(format);
    try {
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
        {isAttendance(type) && (
          <Form.Item label={reportLabels.direction}>
            <Select value={groupType} onChange={setGroupType} options={Object.entries(reportGroupTypeLabels).map(([value, label]) => ({ value, label }))} />
          </Form.Item>
        )}
        {type === 'ATTENDANCE_DAILY' ? (
          <Form.Item label={reportLabels.date}>
            <DatePicker value={range[1]} allowClear={false} format={DISPLAY_DATE_FORMAT} onChange={(d) => d && setRange([d, d])} />
          </Form.Item>
        ) : (
          type !== 'YEARLY_SUMMARY' && type !== 'COURSE_COMPLETION' && type !== 'OTM_ADMISSIONS' && (
            <Form.Item label={reportLabels.period}>
              <DatePicker.RangePicker allowClear={false} value={range} format={DISPLAY_DATE_FORMAT}
                onChange={(v) => v?.[0] && v[1] && setRange([v[0], v[1]])} />
            </Form.Item>
          )
        )}
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
