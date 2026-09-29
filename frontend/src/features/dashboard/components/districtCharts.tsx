import { theme } from 'antd';
import {
  Bar, BarChart, CartesianGrid, LabelList, Legend, ResponsiveContainer, Scatter, ScatterChart, Tooltip, XAxis, YAxis, ZAxis,
} from 'recharts';
import { useChartTheme } from '@/hooks/useChartTheme';
import { chartColors } from '../chartColors';
import { dashboardLabels } from '../labels';
import type { DistrictRow } from '../types';
import { shorten } from '../utils/chartData';

const t = dashboardLabels.districtBreakdown;
const LABEL_WIDTH = 110;
const LABEL_MAX_LENGTH = 18;
const SEGMENT_GAP = 2;
const MAX_TOOLTIP_PROGRAMS = 4;
const BUBBLE_SIZE_RANGE: [number, number] = [120, 900];

/** Kategorik ranglar tartib bilan beriladi; "biriktirilmagan" — hech bir kursga kirmaganlar, shuning uchun neytral kulrang. */
const compositionSeries = [
  { key: 'vocational', name: t.vocational, color: chartColors.primary },
  { key: 'otm', name: t.otm, color: chartColors.accent },
  { key: 'unassigned', name: t.unassigned, color: chartColors.muted },
] as const;

const countSeries = [
  { key: 'professions', name: t.professions, color: chartColors.primary },
  { key: 'institutions', name: t.institutions, color: chartColors.secondary },
] as const;

type DistrictChartProps = { rows: DistrictRow[] };

const districtLabel = (name: string) => shorten(name, LABEL_MAX_LENGTH);

/** Tooltip tarkibi: okrug nomi va kasb–muassasa juftliklari bo'yicha eng katta o'qitish yo'nalishlari. */
function ProgramsTooltip({ active, payload }: { active?: boolean; payload?: { payload: DistrictRow }[] }) {
  const chartTheme = useChartTheme();
  const row = payload?.[0]?.payload;
  if (!active || !row) return null;
  return (
    <div style={{ ...chartTheme.tooltip, padding: '6px 10px', maxWidth: 260 }}>
      <strong>{row.name}</strong>
      <div>{t.professions}: {row.professions} · {t.institutions}: {row.institutions}</div>
      <div>{t.bubbleSize}: {row.vocational}</div>
      {row.programs.slice(0, MAX_TOOLTIP_PROGRAMS).map((program) => (
        <div key={`${program.profession}-${program.institution}`} style={{ opacity: 0.75 }}>
          {program.profession} — {program.institution}: {program.soldiers}
        </div>
      ))}
    </div>
  );
}

/** Tarkib: har okrugda askarlar kasbga, OTMga va biriktirilmaganlarga bo'lingan (yig'ma gorizontal ustun). */
export function CompositionChart({ rows }: DistrictChartProps) {
  const chartTheme = useChartTheme();
  const { token } = theme.useToken();
  const data = rows.map((row) => ({
    name: row.name, vocational: row.vocational, otm: row.otm,
    unassigned: Math.max(0, row.soldiers - row.vocational - row.otm),
  }));
  return (
    <ResponsiveContainer width="100%" height="100%">
      <BarChart data={data} layout="vertical" margin={{ top: 4, right: 16, bottom: 0, left: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke={chartTheme.grid} horizontal={false} />
        <XAxis type="number" allowDecimals={false} tick={chartTheme.tick} />
        <YAxis type="category" dataKey="name" width={LABEL_WIDTH} tick={chartTheme.tick} tickFormatter={districtLabel} />
        <Tooltip contentStyle={chartTheme.tooltip} cursor={chartTheme.cursor} />
        <Legend iconSize={8} wrapperStyle={{ fontSize: 11 }} />
        {compositionSeries.map((series) => (
          <Bar key={series.key} stackId="soldiers" isAnimationActive={false} dataKey={series.key} name={series.name}
            fill={series.color} stroke={token.colorBgContainer} strokeWidth={SEGMENT_GAP / 2}>
            <LabelList dataKey={series.key} position="center" fill="#fff" fontSize={10}
              formatter={(value: number) => (value > 0 ? value : '')} />
          </Bar>
        ))}
      </BarChart>
    </ResponsiveContainer>
  );
}

/** Kasb va muassasa: har okrugda nechta kasb va nechta muassasa bilan o'qitilayotgani (yonma-yon ustunlar). */
export function ProgramCountsChart({ rows }: DistrictChartProps) {
  const chartTheme = useChartTheme();
  return (
    <ResponsiveContainer width="100%" height="100%">
      <BarChart data={rows} margin={{ top: 16, right: 8, bottom: 0, left: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke={chartTheme.grid} vertical={false} />
        <XAxis dataKey="name" tick={chartTheme.tick} tickFormatter={districtLabel} interval={0} />
        <YAxis allowDecimals={false} tick={chartTheme.tick} width={28} />
        <Tooltip content={<ProgramsTooltip />} cursor={chartTheme.cursor} />
        <Legend iconSize={8} wrapperStyle={{ fontSize: 11 }} />
        {countSeries.map((series) => (
          <Bar key={series.key} isAnimationActive={false} dataKey={series.key} name={series.name} fill={series.color}
            radius={[4, 4, 0, 0]}>
            <LabelList dataKey={series.key} position="top" fill={chartTheme.text} fontSize={10} />
          </Bar>
        ))}
      </BarChart>
    </ResponsiveContainer>
  );
}

/** Solishtirish: x — kasblar soni, y — muassasalar soni, pufak o'lchami — kasbdagi askarlar soni. */
export function BubbleChart({ rows }: DistrictChartProps) {
  const chartTheme = useChartTheme();
  return (
    <ResponsiveContainer width="100%" height="100%">
      <ScatterChart margin={{ top: 12, right: 24, bottom: 16, left: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke={chartTheme.grid} />
        <XAxis type="number" dataKey="professions" name={t.bubbleAxisX} allowDecimals={false} tick={chartTheme.tick}
          label={{ value: t.bubbleAxisX, position: 'insideBottom', offset: -8, fill: chartTheme.text, fontSize: 11 }} />
        <YAxis type="number" dataKey="institutions" name={t.bubbleAxisY} allowDecimals={false} tick={chartTheme.tick}
          width={28} />
        <ZAxis type="number" dataKey="vocational" range={BUBBLE_SIZE_RANGE} name={t.bubbleSize} />
        <Tooltip content={<ProgramsTooltip />} cursor={{ strokeDasharray: '3 3' }} />
        <Scatter isAnimationActive={false} data={rows} fill={chartColors.primary} fillOpacity={0.6}
          stroke={chartColors.primary}>
          <LabelList dataKey="name" position="top" fill={chartTheme.text} fontSize={10} formatter={districtLabel} />
        </Scatter>
      </ScatterChart>
    </ResponsiveContainer>
  );
}
