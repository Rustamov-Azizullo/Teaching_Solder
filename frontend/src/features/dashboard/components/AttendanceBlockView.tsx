import { Col, Row } from 'antd';
import { KpiCard } from '@/components/ui';
import { formatPercent } from '@/utils/format';
import { dashboardLabels } from '../labels';
import type { AttendanceBlock, BreakdownRow } from '../types';
import { BreakdownChart, DailyChart, ReasonsChart, WeeklyChart } from './AttendanceCharts';

type AttendanceBlockViewProps = { block: AttendanceBlock; onDrill: (row: BreakdownRow) => void };

export function AttendanceBlockView({ block, onDrill }: AttendanceBlockViewProps) {
  const { summary } = block;
  return (
    <>
      <Row gutter={[12, 12]} style={{ marginBottom: 12 }}>
        <Col xs={12} md={6}><KpiCard title={dashboardLabels.kpi.soldiers} value={summary.totalSoldiers} /></Col>
        <Col xs={12} md={6}><KpiCard title={dashboardLabels.kpi.groups} value={summary.totalGroups} /></Col>
        <Col xs={12} md={6}><KpiCard title={dashboardLabels.kpi.today} value={formatPercent(summary.todayPercent)} /></Col>
        <Col xs={12} md={6}>
          <KpiCard title={dashboardLabels.kpi.missing} value={summary.groupsWithoutAttendanceToday} warning={summary.groupsWithoutAttendanceToday > 0} />
        </Col>
      </Row>
      <Row gutter={[12, 12]}>
        <Col xs={24} xl={12}><DailyChart data={block.daily} /></Col>
        <Col xs={24} xl={12}><WeeklyChart data={block.weekly} /></Col>
        <Col xs={24} xl={14}><BreakdownChart breakdown={block.breakdown} onDrill={onDrill} /></Col>
        <Col xs={24} xl={10}><ReasonsChart data={block.absenceReasons} /></Col>
      </Row>
    </>
  );
}
