import { Alert, Button, Card, Input, Space, Tag, Typography } from 'antd';
import { QRCodeSVG } from 'qrcode.react';
import { useState } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { notify } from '@/lib/notify';
import { authApi } from '../api/authApi';
import { useAuth } from '../hooks/useAuth';
import { authLabels } from '../labels';
import type { TwoFactorSetup } from '../types';

const t = authLabels.twoFactor;

export function TwoFactorPanel() {
  const { user, setupRequired, refreshUser } = useAuth();
  const [setup, setSetup] = useState<TwoFactorSetup | null>(null);
  const [code, setCode] = useState('');
  const [isBusy, setBusy] = useState(false);

  const run = async (action: () => Promise<void>, successMessage: string) => {
    setBusy(true);
    try {
      await action();
      await refreshUser();
      notify.success(successMessage);
      setSetup(null);
      setCode('');
    } catch (error) {
      notify.error(getErrorMessage(error));
    } finally {
      setBusy(false);
    }
  };

  const startSetup = async () => {
    try {
      setSetup(await authApi.setupTwoFactor());
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Card title={t.title} style={{ maxWidth: 520 }}>
      <Space direction="vertical" size="middle" style={{ width: '100%' }}>
        {setupRequired && <Alert type="warning" showIcon message={t.setupRequired} />}
        <Tag color={user?.twoFactorEnabled ? 'green' : 'default'}>{user?.twoFactorEnabled ? t.enabled : t.disabled}</Tag>
        {user?.twoFactorEnabled ? (
          <Space wrap>
            <Input placeholder={t.code} value={code} maxLength={6} onChange={(e) => setCode(e.target.value)} style={{ width: 140 }} />
            <Button danger loading={isBusy} disabled={code.length !== 6} onClick={() => run(() => authApi.disableTwoFactor(code), t.disabledMessage)}>
              {t.disable}
            </Button>
          </Space>
        ) : setup ? (
          <Space direction="vertical" style={{ width: '100%' }}>
            <Typography.Text>{t.scan}</Typography.Text>
            <QRCodeSVG value={setup.otpauthUri} size={168} />
            <Typography.Text copyable code>{setup.secret}</Typography.Text>
            <Space wrap>
              <Input placeholder={t.code} value={code} maxLength={6} inputMode="numeric" onChange={(e) => setCode(e.target.value)} style={{ width: 140 }} />
              <Button type="primary" loading={isBusy} disabled={code.length !== 6} onClick={() => run(() => authApi.enableTwoFactor(code), t.enabledMessage)}>
                {t.confirm}
              </Button>
            </Space>
          </Space>
        ) : (
          <Button type="primary" onClick={startSetup}>{t.start}</Button>
        )}
      </Space>
    </Card>
  );
}
