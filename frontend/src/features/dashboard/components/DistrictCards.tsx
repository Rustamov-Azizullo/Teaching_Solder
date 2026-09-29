import { ApartmentOutlined, BarChartOutlined, DotChartOutlined } from '@ant-design/icons';
import { ChartCard } from '@/components/ui';
import { dashboardLabels } from '../labels';
import type { DistrictRow } from '../types';
import { BubbleChart, CompositionChart, ProgramCountsChart } from './districtCharts';

const t = dashboardLabels.districtBreakdown;

/** Okruglar kesimi kartalari (SuperAdmin/MegaSuperAdmin): har biri alohida karta, bitta qatorda joylashadi. */
export function DistrictCards({ rows }: { rows: DistrictRow[] }) {
  const isEmpty = rows.length === 0;
  return (
    <div className="dashboard-districts">
      <ChartCard title={t.compositionTitle} icon={<BarChartOutlined />} isEmpty={isEmpty}><CompositionChart rows={rows} /></ChartCard>
      <ChartCard title={t.programsTitle} icon={<ApartmentOutlined />} isEmpty={isEmpty}><ProgramCountsChart rows={rows} /></ChartCard>
      <ChartCard title={t.bubbleTitle} icon={<DotChartOutlined />} isEmpty={isEmpty}><BubbleChart rows={rows} /></ChartCard>
    </div>
  );
}
