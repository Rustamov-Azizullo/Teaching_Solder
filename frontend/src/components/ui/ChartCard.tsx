import { Card, Empty } from 'antd';
import type { ReactNode } from 'react';
import { common } from '@/lib/i18n';

type ChartCardProps = { title: string; isEmpty?: boolean; extra?: ReactNode; children: ReactNode };

export function ChartCard({ title, isEmpty, extra, children }: ChartCardProps) {
  return (
    <Card title={title} extra={extra} size="small" style={{ height: '100%' }}>
      {isEmpty ? <Empty description={common.states.empty} style={{ padding: 32 }} /> : children}
    </Card>
  );
}
