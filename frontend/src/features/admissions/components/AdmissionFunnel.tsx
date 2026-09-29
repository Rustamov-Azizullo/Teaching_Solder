import { Funnel, FunnelChart, LabelList, ResponsiveContainer, Tooltip } from 'recharts';
import { ChartCard } from '@/components/ui';
import { useChartTheme } from '@/hooks/useChartTheme';
import { useFunnel } from '../hooks/useAdmissions';
import { admissionLabels } from '../labels';

const FUNNEL_COLORS = ['#3b6fe8', '#0891b2', '#f59e0b', '#16a34a'];

/** Voronka: nomzodlar → BMBAda ro'yxatdan o'tganlar → testda qatnashganlar → qabul qilinganlar. */
export function AdmissionFunnel() {
  const { data, isLoading, error, refetch } = useFunnel();
  const chartTheme = useChartTheme();
  const steps = (data ?? []).map((step, index) => ({
    ...step,
    fill: FUNNEL_COLORS[index % FUNNEL_COLORS.length],
  }));

  return (
    <ChartCard
      title={admissionLabels.funnel}
      isLoading={isLoading}
      error={error}
      onRetry={() => void refetch()}
      isEmpty={steps.every((step) => step.count === 0)}
    >
      <ResponsiveContainer width="100%" height="100%">
        <FunnelChart margin={{ right: 8 }}>
          <Tooltip contentStyle={chartTheme.tooltip} />
          <Funnel dataKey="count" nameKey="label" data={steps} isAnimationActive={false}>
            <LabelList position="inside" dataKey="count" fill="#fff" stroke="none" fontSize={13} />
            <LabelList position="right" dataKey="label" fill={chartTheme.text} stroke="none" fontSize={11} />
          </Funnel>
        </FunnelChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
