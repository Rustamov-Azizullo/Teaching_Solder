import { Col, Row } from 'antd';
import { AdmissionFunnel } from '@/features/admissions';
import { KpiCard } from '@/components/ui';
import { dashboardLabels } from '../labels';
import type { DistrictSoldiers, SubjectNeed } from '../types';
import { DistrictSoldiersCharts } from './DistrictSoldiersCharts';
import { SubjectNeedsChart } from './SurveyCharts';

type OtmGeographyViewProps = { districts: DistrictSoldiers[]; subjectNeeds: SubjectNeed[] };

/** OTM tayyorlov bloki: nomzodlarning okrug/qism taqsimoti, qabul voronkasi va fanlar ehtiyoji. */
export function OtmGeographyView({ districts, subjectNeeds }: OtmGeographyViewProps) {
  const candidates = districts.reduce((sum, district) => sum + district.soldiers, 0);
  return (
    <>
      <Row gutter={[12, 12]} style={{ marginBottom: 12 }}>
        <Col xs={12} md={6}><KpiCard title={dashboardLabels.kpi.otmCandidates} value={candidates} /></Col>
        <Col xs={12} md={6}><KpiCard title={dashboardLabels.kpi.districts} value={districts.length} /></Col>
      </Row>
      <div style={{ marginBottom: 12 }}>
        <DistrictSoldiersCharts districts={districts} title={dashboardLabels.charts.otmByDistrict} />
      </div>
      <Row gutter={[12, 12]}>
        <Col xs={24} xl={12}><AdmissionFunnel /></Col>
        <Col xs={24} xl={12}><SubjectNeedsChart data={subjectNeeds} /></Col>
      </Row>
    </>
  );
}
