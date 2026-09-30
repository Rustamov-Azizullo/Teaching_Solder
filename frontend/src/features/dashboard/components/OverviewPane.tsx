import {
  BankOutlined, BookOutlined, HourglassOutlined, PieChartOutlined, SafetyCertificateOutlined, TeamOutlined, ToolOutlined,
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
    { title: t.kpi.certified, value: data.certified, icon: <SafetyCertificateOutlined /> },
    { title: t.kpi.higher, value: data.higherCompleted, icon: <BankOutlined /> },
    { title: t.kpi.higherIncomplete, value: data.higherIncomplete, icon: <HourglassOutlined /> },
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
