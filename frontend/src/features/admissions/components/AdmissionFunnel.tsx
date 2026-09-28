import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { ChartCard, QueryBoundary } from '@/components/ui';
import { useFunnel } from '../hooks/useAdmissions';
import { admissionLabels } from '../labels';

const COLORS = ['#1677ff', '#13c2c2', '#fa8c16', '#52c41a'];
const HEIGHT = 240;
const LABEL_WIDTH = 190;

/** Voronka: nomzodlar → BMBAda ro'yxatdan o'tganlar → testda qatnashganlar → qabul qilinganlar. */
export function AdmissionFunnel() {
  const { data, isLoading, error, refetch } = useFunnel();
  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {(steps) => (
        <ChartCard title={admissionLabels.funnel} isEmpty={steps.every((s) => s.count === 0)}>
          <ResponsiveContainer width="100%" height={HEIGHT}>
            <BarChart data={steps} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" horizontal={false} />
              <XAxis type="number" allowDecimals={false} />
              <YAxis type="category" dataKey="label" width={LABEL_WIDTH} tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="count" name="Askarlar" radius={[0, 4, 4, 0]}>
                {steps.map((step, index) => <Cell key={step.label} fill={COLORS[index % COLORS.length]} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>
      )}
    </QueryBoundary>
  );
}
