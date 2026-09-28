import { Col, Row } from 'antd';
import { KpiCard } from '@/components/ui';
import { dashboardLabels } from '../labels';
import type { SurveyBlock } from '../types';
import { CompletionChart, HorizontalCountChart, PlansChart, SubjectNeedsChart } from './SurveyCharts';

export function SurveyBlockView({ block }: { block: SurveyBlock }) {
  return (
    <>
      <Row gutter={[12, 12]} style={{ marginBottom: 12 }}>
        <Col xs={12} md={6}><KpiCard title={dashboardLabels.kpi.soldiers} value={block.totalSoldiers} /></Col>
        <Col xs={12} md={6}><KpiCard title={dashboardLabels.kpi.finalized} value={block.finalizedQuestionnaires} /></Col>
      </Row>
      <Row gutter={[12, 12]}>
        <Col xs={24} xl={12}><CompletionChart data={block.completion} /></Col>
        <Col xs={24} xl={12}><PlansChart data={block.futurePlans} /></Col>
        <Col xs={24} xl={12}><HorizontalCountChart title={dashboardLabels.charts.interests} data={block.interests} /></Col>
        <Col xs={24} xl={12}><SubjectNeedsChart data={block.subjectNeeds} /></Col>
        <Col xs={24} xl={12}><HorizontalCountChart title={dashboardLabels.charts.education} data={[...block.education.educationLevels, ...block.education.certificatesAndAwards]} /></Col>
      </Row>
    </>
  );
}
