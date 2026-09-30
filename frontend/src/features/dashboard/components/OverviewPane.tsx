import {
  ApartmentOutlined, BookOutlined, PieChartOutlined, ReadOutlined, SolutionOutlined, TeamOutlined, ToolOutlined,
} from '@ant-design/icons';
import { ChartCard } from '@/components/ui';
import { useAuth, useCan, type Role } from '@/features/auth';
import { useOverview } from '../hooks/useDashboard';
import { dashboardLabels } from '../labels';
import { DonutCard } from './charts/DonutCard';
import { DistrictCards, type BreakdownLevel } from './DistrictCards';
import { KpiGrid, type KpiItem } from './KpiGrid';
import { ProfessionTable } from './ProfessionTable';
import { RegionSoldiersCard } from './RegionSoldiersCard';

const t = dashboardLabels;

const breakdownLevelByRole: Record<Role, BreakdownLevel> = {
  MEGA_SUPER_ADMIN: 'district', SUPER_ADMIN: 'district', ADMIN: 'unit', USER: 'subdivision',
};

/** Yagona dashboard: umumiy raqamlar, yo'nalishlar ulushi, kasblar kesimi va viloyatlar bo'yicha askarlar. */
export function OverviewPane() {
  const { data, isLoading, error, refetch } = useOverview({});
  const { user } = useAuth();
  const canOpenSoldiers = useCan('soldierRead');
  const canOpenGroups = useCan('groupRead');
  const canOpenDictionaries = useCan('dictionaryWrite');
  const isRepublicLevel = user?.role === 'SUPER_ADMIN' || user?.role === 'MEGA_SUPER_ADMIN';
  // Super rollar umumiy ma'lumotnomaga, Admin va User o'z doirasidagi qismlar ro'yxatiga o'tadi.
  const catalogLink = isRepublicLevel ? (canOpenDictionaries ? '/dictionaries' : undefined) : '/catalog';
  const breakdownLevel = user ? breakdownLevelByRole[user.role] : undefined;

  if (!data) {
    return (
      <div className="dashboard-pane">
        <div className="dashboard-body">
          <ChartCard title={t.title} isLoading={isLoading} error={error} onRetry={refetch}>{null}</ChartCard>
        </div>
      </div>
    );
  }

  const kpis: KpiItem[] = [
    { title: t.kpi.total, value: data.totalSoldiers, icon: <TeamOutlined />, to: canOpenSoldiers ? '/soldiers' : undefined },
    { title: t.kpi.vocational, value: data.vocationalStudying, icon: <ToolOutlined />, to: canOpenGroups ? '/groups?type=VOCATIONAL' : undefined },
    { title: t.kpi.otm, value: data.otmPreparing, icon: <BookOutlined />, to: canOpenGroups ? '/groups?type=OTM_PREP' : undefined },
    { title: t.kpi.directions, value: data.catalog.directions, icon: <ApartmentOutlined />, to: catalogLink },
    { title: t.kpi.subjects, value: data.catalog.subjects, icon: <ReadOutlined />, to: catalogLink },
    { title: t.kpi.professions, value: data.catalog.professions, icon: <SolutionOutlined />, to: catalogLink },
  ];
  const directions = [
    { id: 'vocational', name: t.directionNames.vocational, value: data.vocationalStudying },
    { id: 'otm', name: t.directionNames.otm, value: data.otmPreparing },
    { id: 'unassigned', name: t.directionNames.unassigned, value: data.unassigned },
  ];

  const directionsCard = <DonutCard title={t.directions} icon={<PieChartOutlined />} seriesName={t.series.soldiers} data={directions} />;

  return (
    <div className="dashboard-pane">
      <KpiGrid items={kpis} />
      {breakdownLevel && <div className="dashboard-overview-row"><DistrictCards rows={data.districts} level={breakdownLevel} /></div>}
      <div className="dashboard-body">
        <div className="dashboard-col">
          <RegionSoldiersCard rows={data.regions} />
        </div>
        <div className="dashboard-col dashboard-col--full">
          <div className="dashboard-split">
            {directionsCard}
            <ProfessionTable rows={data.professions} bySubdivision={breakdownLevel === 'subdivision'} />
          </div>
        </div>
      </div>
    </div>
  );
}
