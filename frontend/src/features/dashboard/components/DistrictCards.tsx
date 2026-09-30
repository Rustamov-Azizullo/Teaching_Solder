import { ApartmentOutlined, BarChartOutlined, DotChartOutlined } from '@ant-design/icons';
import { ChartCard } from '@/components/ui';
import { dashboardLabels } from '../labels';
import type { DistrictRow } from '../types';
import { BubbleChart, CompositionChart, ProgramCountsChart } from './districtCharts';

const titles = dashboardLabels.districtBreakdown.titles;

export type BreakdownLevel = keyof typeof titles;

/** Kesim kartalari: okruglar (SuperAdmin/MegaSuperAdmin), harbiy qismlar (Admin) yoki bo'linmalar (User); bitta qatorda joylashadi. */
export function DistrictCards({ rows, level }: { rows: DistrictRow[]; level: BreakdownLevel }) {
  const t = titles[level];
  const isEmpty = rows.length === 0;
  return (
    <div className="dashboard-districts">
      <ChartCard title={t.composition} icon={<BarChartOutlined />} isEmpty={isEmpty}><CompositionChart rows={rows} /></ChartCard>
      <ChartCard title={t.programs} icon={<ApartmentOutlined />} isEmpty={isEmpty}><ProgramCountsChart rows={rows} /></ChartCard>
      <ChartCard title={t.bubble} icon={<DotChartOutlined />} isEmpty={isEmpty}><BubbleChart rows={rows} /></ChartCard>
    </div>
  );
}
