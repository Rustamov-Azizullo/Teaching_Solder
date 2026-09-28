import { Space, Typography } from 'antd';
import type { ReactNode } from 'react';

type PageHeaderProps = { title: string; subtitle?: string; actions?: ReactNode };

export function PageHeader({ title, subtitle, actions }: PageHeaderProps) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12, flexWrap: 'wrap', marginBottom: 16 }}>
      <div>
        <Typography.Title level={3} style={{ margin: 0 }}>{title}</Typography.Title>
        {subtitle && <Typography.Text type="secondary">{subtitle}</Typography.Text>}
      </div>
      {actions && <Space wrap>{actions}</Space>}
    </div>
  );
}
