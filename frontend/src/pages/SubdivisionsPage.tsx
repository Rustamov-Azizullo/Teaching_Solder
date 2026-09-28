import { PageHeader } from '@/components/ui';
import { SubdivisionManager, organizationLabels } from '@/features/organization';

export function SubdivisionsPage() {
  return (
    <>
      <PageHeader title={organizationLabels.subdivisions.title} subtitle={organizationLabels.subdivisions.subtitle} />
      <SubdivisionManager />
    </>
  );
}
