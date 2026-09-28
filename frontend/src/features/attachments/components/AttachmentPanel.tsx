import { DeleteOutlined, DownloadOutlined, UploadOutlined } from '@ant-design/icons';
import { Button, Empty, Input, List, Popconfirm, Space, Typography, Upload } from 'antd';
import { useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { useCan } from '@/features/auth';
import { getErrorMessage } from '@/lib/apiClient';
import { notify } from '@/lib/notify';
import { formatDateTime } from '@/utils/format';
import { attachmentApi } from '../api/attachmentApi';
import { useAttachments, useRemoveAttachment, useUploadAttachment } from '../hooks/useAttachments';
import { attachmentLabels } from '../labels';
import type { AttachmentOwnerType } from '../types';

const MAX_BYTES = 10 * 1024 * 1024;
const KB = 1024;

type AttachmentPanelProps = { ownerType: AttachmentOwnerType; ownerId: number; title?: string };

export function AttachmentPanel({ ownerType, ownerId, title = attachmentLabels.title }: AttachmentPanelProps) {
  const canWrite = useCan('attachmentWrite');
  const [kind, setKind] = useState('');
  const { data, isLoading, error, refetch } = useAttachments(ownerType, ownerId);
  const upload = useUploadAttachment(ownerType, ownerId);
  const remove = useRemoveAttachment(ownerType, ownerId);

  const handleUpload = async (file: File) => {
    if (file.size > MAX_BYTES) {
      notify.error(attachmentLabels.tooLarge);
      return false;
    }
    try {
      await upload.mutateAsync({ file, kind: kind || undefined });
      notify.success(attachmentLabels.uploaded);
      setKind('');
    } catch (err) {
      notify.error(getErrorMessage(err));
    }
    return false;
  };

  return (
    <Space direction="vertical" size="small" style={{ width: '100%' }}>
      <Typography.Text strong>{title}</Typography.Text>
      {canWrite && (
        <Space wrap>
          <Input placeholder={attachmentLabels.kindPlaceholder} value={kind} onChange={(e) => setKind(e.target.value)} style={{ width: 240 }} />
          <Upload showUploadList={false} accept=".pdf,.jpg,.jpeg,.png" beforeUpload={handleUpload}>
            <Button icon={<UploadOutlined />} loading={upload.isPending}>{attachmentLabels.upload}</Button>
          </Upload>
          <Typography.Text type="secondary">{attachmentLabels.accepted}</Typography.Text>
        </Space>
      )}
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(items) =>
          items.length === 0 ? (
            <Empty description={attachmentLabels.empty} image={Empty.PRESENTED_IMAGE_SIMPLE} />
          ) : (
            <List
              size="small"
              dataSource={items}
              renderItem={(item) => (
                <List.Item
                  actions={[
                    <Button key="d" type="link" icon={<DownloadOutlined />} onClick={() => attachmentApi.download(item)}>
                      {attachmentLabels.download}
                    </Button>,
                    ...(canWrite
                      ? [
                          <Popconfirm key="x" title="O'chirilsinmi?" onConfirm={() => remove.mutate(item.id)}>
                            <Button type="text" danger icon={<DeleteOutlined />} aria-label="O'chirish" />
                          </Popconfirm>,
                        ]
                      : []),
                  ]}
                >
                  <List.Item.Meta
                    title={item.kind ? `${item.kind} — ${item.fileName}` : item.fileName}
                    description={`${Math.ceil(item.sizeBytes / KB)} KB · ${item.uploadedBy} · ${formatDateTime(item.uploadedAt)}`}
                  />
                </List.Item>
              )}
            />
          )
        }
      </QueryBoundary>
    </Space>
  );
}
