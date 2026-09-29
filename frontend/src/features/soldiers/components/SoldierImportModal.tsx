import { DownloadOutlined, InboxOutlined } from '@ant-design/icons';
import { Alert, Button, Modal, Space, Upload } from 'antd';
import { useState } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { soldierApi } from '../api/soldierApi';
import { useImportSoldiers } from '../hooks/useSoldiers';
import { soldierLabels } from '../labels';
import type { ImportResult } from '../types';
import { NumberedTable } from '@/components/ui';

const t = soldierLabels.import;

export function SoldierImportModal({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { mutateAsync, isPending } = useImportSoldiers();
  const [result, setResult] = useState<ImportResult | null>(null);

  const handleFile = async (file: File) => {
    try {
      setResult(await mutateAsync(file));
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
    return false;
  };

  return (
    <Modal open={open} title={t.title} onCancel={() => { setResult(null); onClose(); }} footer={<Button onClick={onClose}>{common.actions.close}</Button>} width={640}>
      <Space direction="vertical" size="middle" style={{ width: '100%' }}>
        <Button icon={<DownloadOutlined />} onClick={() => soldierApi.downloadTemplate()}>{t.template}</Button>
        <Upload.Dragger accept=".xlsx" showUploadList={false} beforeUpload={handleFile} disabled={isPending}>
          <p className="ant-upload-drag-icon"><InboxOutlined /></p>
          <p>{t.pick}</p>
        </Upload.Dragger>
        {result && (
          <>
            <Alert type={result.errors.length ? 'warning' : 'success'} showIcon message={t.result(result.imported, result.errors.length)} />
            {result.errors.length > 0 && (
              <NumberedTable
                size="small"
                rowKey="row"
                pagination={{ pageSize: 8, hideOnSinglePage: true }}
                dataSource={result.errors}
                columns={[{ title: t.row, dataIndex: 'row', width: 80 }, { title: t.message, dataIndex: 'message' }]}
              />
            )}
          </>
        )}
      </Space>
    </Modal>
  );
}
