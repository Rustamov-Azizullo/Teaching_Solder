import {
  Bar, BarChart, CartesianGrid, Cell, ComposedChart, Legend, Line, LineChart, Pie, PieChart, ResponsiveContainer,
  Tooltip, XAxis, YAxis,
} from 'recharts';
import { ChartCard } from '@/components/ui';
import { DISPLAY_DATE_FORMAT, dayjs } from '@/lib/dayjs';
import { formatPercent } from '@/utils/format';
import { CHART_HEIGHT, chartColors } from '../chartColors';
import { dashboardLabels } from '../labels';
import type { Breakdown, BreakdownRow, DailyPoint, ReasonSlice, WeeklyPoint } from '../types';

const PERCENT_DOMAIN: [number, number] = [0, 100];
const SHORT_DATE_FORMAT = 'DD.MM';
const BREAKDOWN_ROW_HEIGHT = 36;
const MIN_BREAKDOWN_HEIGHT = 140;
const LABEL_WIDTH = 150;

export function DailyChart({ data }: { data: DailyPoint[] }) {
  return (
    <ChartCard title={dashboardLabels.charts.daily} isEmpty={data.length === 0}>
      <ResponsiveContainer width="100%" height={CHART_HEIGHT}>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" tickFormatter={(value: string) => dayjs(value).format(SHORT_DATE_FORMAT)} />
          <YAxis domain={PERCENT_DOMAIN} unit="%" />
          <Tooltip labelFormatter={(value) => dayjs(String(value)).format(DISPLAY_DATE_FORMAT)} formatter={(value) => formatPercent(Number(value))} />
          <Line type="monotone" dataKey="percent" name="Davomat" stroke={chartColors.primary} strokeWidth={2} dot={false} />
        </LineChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}

export function WeeklyChart({ data }: { data: WeeklyPoint[] }) {
  return (
    <ChartCard title={dashboardLabels.charts.weekly} isEmpty={data.length === 0}>
      <ResponsiveContainer width="100%" height={CHART_HEIGHT}>
        <ComposedChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="weekStart" tickFormatter={(value: string) => dayjs(value).format(SHORT_DATE_FORMAT)} />
          <YAxis yAxisId="percent" domain={PERCENT_DOMAIN} unit="%" />
          <YAxis yAxisId="hours" orientation="right" />
          <Tooltip labelFormatter={(value) => `${dayjs(String(value)).format(DISPLAY_DATE_FORMAT)} haftasi`} />
          <Legend />
          <Bar yAxisId="percent" dataKey="averagePercent" name={dashboardLabels.charts.weeklyPercent} fill={chartColors.primary} />
          <Line yAxisId="hours" type="monotone" dataKey="academicHours" name={dashboardLabels.charts.weeklyHours} stroke={chartColors.accent} strokeWidth={2} />
        </ComposedChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}

type BreakdownChartProps = { breakdown: Breakdown; onDrill: (row: BreakdownRow) => void };

/** Davomat kesimi (gorizontal ustunli). Okrug va qism ustunlari bosilganda pastki darajaga o'tiladi. */
export function BreakdownChart({ breakdown, onDrill }: BreakdownChartProps) {
  const canDrill = breakdown.level !== 'GROUP';
  const height = Math.max(MIN_BREAKDOWN_HEIGHT, breakdown.rows.length * BREAKDOWN_ROW_HEIGHT);
  return (
    <ChartCard
      title={dashboardLabels.charts.breakdown[breakdown.level]}
      isEmpty={breakdown.rows.length === 0}
      extra={canDrill ? <small>{dashboardLabels.drill.hint}</small> : undefined}
    >
      <ResponsiveContainer width="100%" height={height}>
        <BarChart data={breakdown.rows} layout="vertical" margin={{ left: 8 }}>
          <CartesianGrid strokeDasharray="3 3" horizontal={false} />
          <XAxis type="number" domain={PERCENT_DOMAIN} unit="%" />
          <YAxis type="category" dataKey="label" width={LABEL_WIDTH} tick={{ fontSize: 12 }} />
          <Tooltip formatter={(value) => formatPercent(Number(value))} />
          <Bar dataKey="percent" name="Davomat" radius={[0, 4, 4, 0]} cursor={canDrill ? 'pointer' : 'default'}
            onClick={(row: BreakdownRow) => canDrill && onDrill(row)}>
            {breakdown.rows.map((row) => <Cell key={row.id} fill={chartColors.primary} />)}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}

export function ReasonsChart({ data }: { data: ReasonSlice[] }) {
  const visible = data.filter((slice) => slice.count > 0);
  return (
    <ChartCard title={dashboardLabels.charts.reasons} isEmpty={visible.length === 0}>
      <ResponsiveContainer width="100%" height={CHART_HEIGHT}>
        <PieChart>
          <Pie data={visible} dataKey="count" nameKey="label" innerRadius={50} outerRadius={90} label>
            {visible.map((slice, index) => <Cell key={slice.reason} fill={chartColors.series[index % chartColors.series.length]} />)}
          </Pie>
          <Tooltip />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
