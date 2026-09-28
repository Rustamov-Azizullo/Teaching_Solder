import {
  Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts';
import { ChartCard } from '@/components/ui';
import { formatPercent } from '@/utils/format';
import { CHART_HEIGHT, chartColors } from '../chartColors';
import { dashboardLabels } from '../labels';
import type { CompletionRow, CountItem, SubjectNeed } from '../types';

const ROW_HEIGHT = 34;
const MIN_HEIGHT = 140;
const LABEL_WIDTH = 190;
const PERCENT_DOMAIN: [number, number] = [0, 100];

export function CompletionChart({ data }: { data: CompletionRow[] }) {
  return (
    <ChartCard title={dashboardLabels.charts.completion} isEmpty={data.length === 0}>
      <ResponsiveContainer width="100%" height={CHART_HEIGHT}>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="unitName" tick={{ fontSize: 11 }} />
          <YAxis domain={PERCENT_DOMAIN} unit="%" />
          <Tooltip formatter={(value) => formatPercent(Number(value))} />
          <Bar dataKey="percent" name="To'ldirilgan" fill={chartColors.secondary} />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}

export function HorizontalCountChart({ title, data }: { title: string; data: CountItem[] }) {
  const height = Math.max(MIN_HEIGHT, data.length * ROW_HEIGHT);
  return (
    <ChartCard title={title} isEmpty={data.length === 0}>
      <ResponsiveContainer width="100%" height={height}>
        <BarChart data={data} layout="vertical">
          <CartesianGrid strokeDasharray="3 3" horizontal={false} />
          <XAxis type="number" allowDecimals={false} />
          <YAxis type="category" dataKey="label" width={LABEL_WIDTH} tick={{ fontSize: 11 }} />
          <Tooltip />
          <Bar dataKey="count" name="Askarlar" fill={chartColors.primary} radius={[0, 4, 4, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}

export function PlansChart({ data }: { data: CountItem[] }) {
  return (
    <ChartCard title={dashboardLabels.charts.plans} isEmpty={data.length === 0}>
      <ResponsiveContainer width="100%" height={CHART_HEIGHT + 40}>
        <PieChart>
          <Pie data={data} dataKey="count" nameKey="label" outerRadius={90} label>
            {data.map((item, index) => <Cell key={item.label} fill={chartColors.series[index % chartColors.series.length]} />)}
          </Pie>
          <Tooltip />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}

export function SubjectNeedsChart({ data }: { data: SubjectNeed[] }) {
  return (
    <ChartCard title={dashboardLabels.charts.subjectNeeds} isEmpty={data.length === 0}>
      <ResponsiveContainer width="100%" height={CHART_HEIGHT}>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="subject" tick={{ fontSize: 11 }} />
          <YAxis allowDecimals={false} />
          <Tooltip />
          <Legend />
          <Bar dataKey="specialtyCount" name={dashboardLabels.charts.specialty} fill={chartColors.primary} />
          <Bar dataKey="mandatoryCount" name={dashboardLabels.charts.mandatory} fill={chartColors.accent} />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
