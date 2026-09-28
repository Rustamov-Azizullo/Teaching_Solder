import { PageHeader } from '@/components/ui';
import { LocationsManager, adminLabels } from '@/features/admin';

export function LocationsPage() {
  return (
    <>
      <PageHeader title={adminLabels.locations.title} subtitle={adminLabels.locations.subtitle} />
      <LocationsManager />
    </>
  );
}
