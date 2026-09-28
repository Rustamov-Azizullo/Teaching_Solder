import { Card, Descriptions, Space, Typography } from 'antd';
import { PageHeader } from '@/components/ui';
import { authLabels, useAuth } from '@/features/auth';
import { ThemeModeSwitch } from '@/app/theme/ThemeModeSwitch';
import { common } from '@/lib/i18n';

export function ProfilePage() {
  const { user } = useAuth();
  return (
    <>
      <PageHeader title={authLabels.profileTitle} />
      <Space direction="vertical" size="middle" style={{ display: 'flex', maxWidth: 640 }}>
        <Card>
          <Descriptions column={1} size="small">
            <Descriptions.Item label={authLabels.username}>{user?.username}</Descriptions.Item>
            <Descriptions.Item label={authLabels.fullName}>{user?.fullName}</Descriptions.Item>
            <Descriptions.Item label={authLabels.role}>{user?.roleLabel}</Descriptions.Item>
            {user?.locationName && <Descriptions.Item label={authLabels.location}>{user.locationName}</Descriptions.Item>}
          </Descriptions>
        </Card>
        <Card title={common.theme.title}>
          <Space>
            <Typography.Text>{common.theme.toggle}</Typography.Text>
            <ThemeModeSwitch />
          </Space>
        </Card>
      </Space>
    </>
  );
}
