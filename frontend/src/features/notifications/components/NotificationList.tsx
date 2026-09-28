import { Button, List, Space, Tag, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { QueryBoundary } from '@/components/ui';
import { formatDateTime } from '@/utils/format';
import { useMarkAllRead, useMarkRead, useNotifications } from '../hooks/useNotifications';
import { notificationLabels } from '../labels';

export function NotificationList() {
  const navigate = useNavigate();
  const { data, isLoading, error, refetch } = useNotifications();
  const markRead = useMarkRead();
  const markAll = useMarkAllRead();
  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Button onClick={() => markAll.mutate()} loading={markAll.isPending}>{notificationLabels.markAll}</Button>
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch} isEmpty={(items) => items.length === 0}>
        {(items) => (
          <List bordered dataSource={items}
            renderItem={(item) => (
              <List.Item style={{ cursor: item.link ? 'pointer' : 'default' }}
                onClick={() => { if (!item.read) markRead.mutate(item.id); if (item.link) navigate(item.link); }}>
                <List.Item.Meta
                  title={<Space>{!item.read && <Tag color="blue">Yangi</Tag>}<Typography.Text strong={!item.read}>{item.title}</Typography.Text></Space>}
                  description={item.body} />
                <small>{formatDateTime(item.createdAt)}</small>
              </List.Item>
            )} />
        )}
      </QueryBoundary>
    </Space>
  );
}
