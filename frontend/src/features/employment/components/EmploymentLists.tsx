import { FileExcelOutlined, FilePdfOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Empty, Input, Space, Table, Tabs } from 'antd';
import { useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { getErrorMessage } from '@/lib/apiClient';
import { notify } from '@/lib/notify';
import { formatDate, formatDateTime } from '@/utils/format';
import { useEmploymentHistory, useEmploymentPreview, useExportEmployment } from '../hooks/useEmployment';
import { employmentLabels } from '../labels';
import { statusLabels } from '../statuses';
import type { EmploymentRow, ExportFormat, HistoryRow, RegionGroup } from '../types';

const c = employmentLabels.columns;

function RegionCard({ group, agency }: { group: RegionGroup; agency: string }) {
  const exporter = useExportEmployment();
  const run = async (format: ExportFormat) => {
    if (!agency.trim()) {
      notify.warning(employmentLabels.agencyPlaceholder);
      return;
    }
    try {
      await exporter.mutateAsync({ agency, regionId: group.regionId, format });
      notify.success(employmentLabels.exported);
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };
  return (
    <Card size="small" title={`${group.regionName} (${group.rows.length})`}
      extra={<Space>
        <Button size="small" icon={<FileExcelOutlined />} loading={exporter.isPending} onClick={() => run('XLSX')}>{employmentLabels.exportXlsx}</Button>
        <Button size="small" icon={<FilePdfOutlined />} loading={exporter.isPending} onClick={() => run('PDF')}>{employmentLabels.exportPdf}</Button>
      </Space>}>
      <Table<EmploymentRow> rowKey="soldierId" size="small" pagination={false} scroll={{ x: 'max-content' }} dataSource={group.rows}
        columns={[
          { title: c.name, dataIndex: 'fullName' },
          { title: c.birth, dataIndex: 'birthDate', render: formatDate },
          { title: c.district, dataIndex: 'district' },
          { title: c.profession, dataIndex: 'profession', render: (v: string | null) => v ?? '—' },
          { title: c.status, dataIndex: 'status', render: (s: EmploymentRow['status']) => statusLabels[s] },
          { title: c.cert, dataIndex: 'certificateNo', render: (v: string | null) => v ?? '—' },
        ]} />
    </Card>
  );
}

export function EmploymentLists() {
  const preview = useEmploymentPreview();
  const history = useEmploymentHistory();
  const [agency, setAgency] = useState('');

  return (
    <Tabs items={[
      { key: 'p', label: employmentLabels.tabs.preview, children: (
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <Space.Compact style={{ maxWidth: 640, width: '100%' }}>
            <Button disabled>{employmentLabels.agency}</Button>
            <Input value={agency} onChange={(e) => setAgency(e.target.value)} placeholder={employmentLabels.agencyPlaceholder} />
          </Space.Compact>
          <Alert type="info" showIcon message={employmentLabels.minimal} />
          <QueryBoundary isLoading={preview.isLoading} error={preview.error} data={preview.data} onRetry={preview.refetch}>
            {(groups) => groups.length === 0 ? <Empty description={employmentLabels.empty} /> : (
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                {groups.map((group) => <RegionCard key={group.regionId} group={group} agency={agency} />)}
              </Space>
            )}
          </QueryBoundary>
        </Space>) },
      { key: 'h', label: employmentLabels.tabs.history, children: (
        <QueryBoundary isLoading={history.isLoading} error={history.error} data={history.data} onRetry={history.refetch}>
          {(rows) => (
            <Table<HistoryRow> rowKey="id" size="small" dataSource={rows} scroll={{ x: 'max-content' }} pagination={{ pageSize: 15, hideOnSinglePage: true }}
              columns={[
                { title: c.at, dataIndex: 'generatedAt', render: formatDateTime }, { title: c.region, dataIndex: 'regionName' },
                { title: c.agency, dataIndex: 'agency' }, { title: c.count, dataIndex: 'soldierCount' },
                { title: c.format, dataIndex: 'format' }, { title: c.by, dataIndex: 'generatedBy' },
              ]} />
          )}
        </QueryBoundary>) },
    ]} />
  );
}
