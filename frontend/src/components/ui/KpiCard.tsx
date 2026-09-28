import { Card, Statistic } from 'antd';

type KpiCardProps = { title: string; value: number | string; suffix?: string; warning?: boolean };

export function KpiCard({ title, value, suffix, warning }: KpiCardProps) {
  return (
    <Card size="small">
      <Statistic title={title} value={value} suffix={suffix} valueStyle={warning ? { color: '#d46b08' } : undefined} />
    </Card>
  );
}
