import { PageHeader } from '@/components/ui';
import { FacilityManager, facilityLabels } from '@/features/facilities';

export function FacilitiesPage() {
  return (
    <>
      <PageHeader title={facilityLabels.title} subtitle={facilityLabels.subtitle} />
      <FacilityManager />
    </>
  );
}
