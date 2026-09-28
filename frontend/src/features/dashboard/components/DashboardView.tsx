import { Space, Tabs, type TabsProps } from 'antd';
import { PageHeader, QueryBoundary } from '@/components/ui';
import { useCan } from '@/features/auth';
import { useGeography, useOtmDistricts, useSurveyBlock } from '../hooks/useDashboard';
import { dashboardLabels } from '../labels';
import { CourseResultsChart } from './CourseResultsChart';
import { GeographyView } from './GeographyView';
import { OtmGeographyView } from './OtmGeographyView';
import { SurveyBlockView } from './SurveyBlockView';

export function DashboardView() {
  const canVocational = useCan('dashboardVocational');
  const canOtm = useCan('dashboardOtm');
  const canSurveys = useCan('dashboardSurveys');
  const surveys = useSurveyBlock({}, canSurveys);
  const geography = useGeography({}, canSurveys);
  const otmDistricts = useOtmDistricts({}, canOtm);

  const tabs: NonNullable<TabsProps['items']> = [];
  if (canVocational || canSurveys) {
    tabs.push({
      key: 'vocational',
      label: dashboardLabels.blocks.vocational,
      children: (
        <Space direction="vertical" size="middle" style={{ display: 'flex' }}>
          {canSurveys && (
            <QueryBoundary isLoading={geography.isLoading} error={geography.error} data={geography.data} onRetry={geography.refetch}>
              {(block) => <GeographyView block={block} professionChoices={surveys.data?.interests ?? []} />}
            </QueryBoundary>
          )}
          {canVocational && <CourseResultsChart filters={{}} />}
        </Space>
      ),
    });
  }
  if (canOtm) {
    tabs.push({
      key: 'otm',
      label: dashboardLabels.blocks.otm,
      children: (
        <QueryBoundary isLoading={otmDistricts.isLoading} error={otmDistricts.error} data={otmDistricts.data} onRetry={otmDistricts.refetch}>
          {(districts) => <OtmGeographyView districts={districts} subjectNeeds={surveys.data?.subjectNeeds ?? []} />}
        </QueryBoundary>
      ),
    });
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
