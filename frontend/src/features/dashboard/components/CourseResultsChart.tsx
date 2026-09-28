import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { ChartCard, QueryBoundary } from '@/components/ui';
import { CHART_HEIGHT, chartColors } from '../chartColors';
import { dashboardLabels } from '../labels';
import { useCourseResults } from '../hooks/useDashboard';
import type { DashboardFilters } from '../types';

/** Kurs natijalari: o'qiganlar, imtihondan o'tganlar, sertifikat olganlar (tugatmaganlar bilan). */
export function CourseResultsChart({ filters }: { filters: Pick<DashboardFilters, 'districtId' | 'unitId'> }) {
  const { data, isLoading, error, refetch } = useCourseResults(filters);
  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {(items) => (
        <ChartCard title={dashboardLabels.charts.courseResults} isEmpty={items.every((i) => i.count === 0)}>
          <ResponsiveContainer width="100%" height={CHART_HEIGHT}>
            <BarChart data={items}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="label" />
              <YAxis allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="count" name="Askarlar">
                {items.map((item, index) => <Cell key={item.label} fill={chartColors.series[index % chartColors.series.length]} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>
      )}
    </QueryBoundary>
  );
}
