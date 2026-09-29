import { EnvironmentOutlined } from '@ant-design/icons';
import { Bar, BarChart, CartesianGrid, LabelList, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { ChartCard } from '@/components/ui';
import { useChartTheme } from '@/hooks/useChartTheme';
import { chartColors } from '../chartColors';
import { dashboardLabels } from '../labels';
import type { RegionRow } from '../types';

const t = dashboardLabels;
const LABEL_ANGLE = -35;
const AXIS_LABEL_HEIGHT = 54;
const REGION_SUFFIXES = / (viloyati|Respublikasi)$/;
const CITY_SUFFIX = / shahri$/;

/** O'q yorlig'i uchun qisqa nom: "Andijon viloyati" -> "Andijon", "Toshkent shahri" -> "Toshkent sh.". */
const shortRegionName = (name: string): string => name.replace(REGION_SUFFIXES, '').replace(CITY_SUFFIX, ' sh.');

/** 12 viloyat, Toshkent shahri va Qoraqalpog'iston Respublikasi bo'yicha harbiy xizmat o'tayotgan askarlar soni. */
export function RegionSoldiersCard({ rows }: { rows: RegionRow[] }) {
  const chartTheme = useChartTheme();
  return (
    <ChartCard title={t.regions} icon={<EnvironmentOutlined />} isEmpty={rows.every((row) => row.soldiers === 0)}>
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={rows} margin={{ top: 16, right: 8, bottom: 0, left: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke={chartTheme.grid} vertical={false} />
          <XAxis dataKey="name" interval={0} angle={LABEL_ANGLE} textAnchor="end" height={AXIS_LABEL_HEIGHT}
            tick={chartTheme.tick} tickFormatter={shortRegionName} />
          <YAxis allowDecimals={false} tick={chartTheme.tick} width={28} />
          <Tooltip contentStyle={chartTheme.tooltip} cursor={chartTheme.cursor} />
          <Bar isAnimationActive={false} dataKey="soldiers" name={t.series.soldiers} fill={chartColors.primary}
            radius={[4, 4, 0, 0]}>
            <LabelList dataKey="soldiers" position="top" fill={chartTheme.text} fontSize={10} />
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
