import { Tabs, type TabsProps } from 'antd';
import { PageHeader, QueryBoundary } from '@/components/ui';
import { AdmissionFunnel } from '@/features/admissions';
import { useCan } from '@/features/auth';
import { useSurveyBlock } from '../hooks/useDashboard';
import { dashboardLabels } from '../labels';
import { CourseResultsChart } from './CourseResultsChart';
import { SurveyBlockView } from './SurveyBlockView';

export function DashboardView() {
  const canVocational = useCan('dashboardVocational');
  const canOtm = useCan('dashboardOtm');
  const canSurveys = useCan('dashboardSurveys');
  const surveys = useSurveyBlock({}, canSurveys);

  const tabs: NonNullable<TabsProps['items']> = [];
  if (canVocational) {
    tabs.push({ key: 'vocational', label: dashboardLabels.blocks.vocational, children: <CourseResultsChart filters={{}} /> });
  }
  if (canOtm) {
    tabs.push({ key: 'otm', label: dashboardLabels.blocks.otm, children: <AdmissionFunnel /> });
  }
  if (canSurveys) {
    tabs.push({
      key: 'surveys',
      label: dashboardLabels.blocks.surveys,
      children: (
        <QueryBoundary isLoading={surveys.isLoading} error={surveys.error} data={surveys.data} onRetry={surveys.refetch}>
          {(block) => <SurveyBlockView block={block} />}
        </QueryBoundary>
      ),
    });
  }

  return (
    <>
      <PageHeader title={dashboardLabels.title} subtitle={dashboardLabels.subtitle} />
      <Tabs items={tabs} />
    </>
  );
}
