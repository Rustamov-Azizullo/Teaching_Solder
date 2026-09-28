import { Card, Typography } from 'antd';
import { Navigate, useLocation } from 'react-router-dom';
import { authLabels, LoginForm, useAuth } from '@/features/auth';
import { common } from '@/lib/i18n';

export function LoginPage() {
  const { status } = useAuth();
  const location = useLocation();
  const redirectTo = (location.state as { from?: string } | null)?.from ?? '/';

  if (status === 'authenticated') return <Navigate to={redirectTo} replace />;

  return (
    <div style={{ display: 'grid', placeItems: 'center', minHeight: '100vh', padding: 16 }}>
      <Card style={{ width: '100%', maxWidth: 400 }}>
        <Typography.Title level={3} style={{ marginBottom: 0 }}>{common.appName}</Typography.Title>
        <Typography.Paragraph type="secondary">{authLabels.title}</Typography.Paragraph>
        <LoginForm />
        <Typography.Paragraph type="secondary" style={{ marginTop: 16, fontSize: 12 }}>{authLabels.demoHint}</Typography.Paragraph>
      </Card>
    </div>
  );
}
