import type { ReactNode } from 'react';
import { Cell, Label, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import { ChartCard } from '@/components/ui';
import { chartColors } from '../../chartColors';
import { useChartTheme } from '@/hooks/useChartTheme';
import { dashboardLabels } from '../../labels';
import { shorten, sumValues, takeTop, type NamedValue } from '../../utils/chartData';

const DEFAULT_LIMIT = 6;
const LEGEND_LABEL_LENGTH = 22;
const CENTER_FONT_SIZE = 20;

type DonutCardProps = {
  title: string;
  icon?: ReactNode;
  data: NamedValue[];
  seriesName: string;
  limit?: number;
  onSelect?: (id: NamedValue['id']) => void;
};

/** Halqa (donut) diagramma: markazda jami son, pastda yorliqlar; ulushlarni ko'rsatish uchun. */
export function DonutCard({ title, icon, data, seriesName, limit = DEFAULT_LIMIT, onSelect }: DonutCardProps) {
  const chartTheme = useChartTheme();
  const slices = takeTop(data, limit, dashboardLabels.other);
  return (
    <ChartCard title={title} icon={icon} isEmpty={sumValues(slices) === 0}>
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie isAnimationActive={false}
            data={slices}
            dataKey="value"
            nameKey="name"
            name={seriesName}
            innerRadius="52%"
            outerRadius="80%"
            paddingAngle={2}
            stroke="none"
            style={onSelect ? { cursor: 'pointer' } : undefined}
            onClick={(_, index) => onSelect?.(slices[index].id)}
          >
            {slices.map((slice, index) => <Cell key={slice.id} fill={chartColors.series[index % chartColors.series.length]} />)}
            <Label value={sumValues(slices)} position="center" fill={chartTheme.text} fontSize={CENTER_FONT_SIZE} />
          </Pie>
          <Tooltip contentStyle={chartTheme.tooltip} />
          <Legend
            verticalAlign="bottom"
            iconSize={8}
            wrapperStyle={{ fontSize: 11 }}
            formatter={(name: string) => shorten(name, LEGEND_LABEL_LENGTH)}
          />
        </PieChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
