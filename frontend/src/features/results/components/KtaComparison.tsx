import { CloudSyncOutlined, UploadOutlined } from '@ant-design/icons';
import { Alert, Button, Space, Table, Upload } from 'antd';
import { getErrorMessage } from '@/lib/apiClient';
import { useKtaApi, useKtaFile } from '../hooks/useResults';
import { resultLabels } from '../labels';
import type { Mismatch } from '../types';

const t = resultLabels.kta;

export function KtaComparison() {
  const viaApi = useKtaApi();
  const viaFile = useKtaFile();
  const active = viaFile.data ? viaFile : viaApi;
  const data = active.data;
  const error = viaApi.error ?? viaFile.error;

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Space wrap>
        <Button type="primary" icon={<CloudSyncOutlined />} loading={viaApi.isPending} onClick={() => { viaFile.reset(); viaApi.mutate(); }}>{t.api}</Button>
        <Upload accept=".xlsx" showUploadList={false} beforeUpload={(file) => { viaApi.reset(); viaFile.mutate(file); return false; }}>
          <Button icon={<UploadOutlined />} loading={viaFile.isPending}>{t.file}</Button>
        </Upload>
      </Space>
      {error && <Alert type="error" showIcon message={getErrorMessage(error)} />}
      {data && (data.length === 0 ? <Alert type="success" showIcon message={t.none} /> : (
        <>
          <Alert type="warning" showIcon message={t.found(data.length)} />
          <Table<Mismatch> rowKey="pinfl" size="small" dataSource={data} pagination={{ pageSize: 15, hideOnSinglePage: true }} scroll={{ x: 'max-content' }}
            columns={[
              { title: 'F.I.Sh.', dataIndex: 'fullName' }, { title: 'JShShIR', dataIndex: 'pinfl' }, { title: t.unit, dataIndex: 'unitName' },
              { title: t.ours, dataIndex: 'ourCertificate' }, { title: t.theirs, dataIndex: 'ktaCertificate', render: (v: string | null) => v ?? '—' },
              { title: t.problem, dataIndex: 'problem' },
            ]} />
        </>
      ))}
    </Space>
  );
}
