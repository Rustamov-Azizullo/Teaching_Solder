import { CheckOutlined } from '@ant-design/icons';
import { Button, Card, Segmented, Space, Typography } from 'antd';
import { useMemo, useState } from 'react';
import { QueryBoundary } from '@/components/ui';
import { useMarkAllRead, useNotifications } from '../hooks/useNotifications';
import { useOpenNotification } from '../hooks/useOpenNotification';
import { notificationLabels } from '../labels';
import { groupByDay } from '../utils/groupByDay';
import { NotificationItem } from './NotificationItem';

type Filter = 'all' | 'unread';

export function NotificationList() {
  const { data, isLoading, error, refetch } = useNotifications();
  const markAll = useMarkAllRead();
  const open = useOpenNotification();
  const [filter, setFilter] = useState<Filter>('all');

  const unreadCount = data?.filter((item) => !item.read).length ?? 0;
  const groups = useMemo(
    () => groupByDay(filter === 'unread' ? (data ?? []).filter((item) => !item.read) : (data ?? [])),
    [data, filter],
  );

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%', maxWidth: 760 }}>
      <Space wrap style={{ justifyContent: 'space-between', width: '100%' }}>
        <Segmented<Filter>
          value={filter}
          onChange={setFilter}
          options={[
            { value: 'all', label: notificationLabels.filterAll },
            { value: 'unread', label: `${notificationLabels.filterUnread} (${unreadCount})` },
          ]}
        />
        <Button icon={<CheckOutlined />} onClick={() => markAll.mutate()} loading={markAll.isPending} disabled={unreadCount === 0}>
          {notificationLabels.markAll}
        </Button>
      </Space>
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch} isEmpty={() => groups.length === 0}>
        {() => (
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            {groups.map((group) => (
              <div key={group.key}>
                <Typography.Text type="secondary" strong style={{ display: 'block', margin: '0 4px 6px', fontSize: 12, textTransform: 'uppercase' }}>
                  {notificationLabels.groups[group.key]}
                </Typography.Text>
                <Card size="small" styles={{ body: { padding: 4 } }}>
                  {group.items.map((item) => <NotificationItem key={item.id} item={item} onOpen={open} />)}
                </Card>
              </div>
            ))}
          </Space>
        )}
      </QueryBoundary>
    </Space>
  );
}
