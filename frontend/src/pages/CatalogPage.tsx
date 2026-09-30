import { PageHeader } from '@/components/ui';
import { CatalogView, dashboardLabels } from '@/features/dashboard';

export function CatalogPage() {
  return (
    <>
      <PageHeader title={dashboardLabels.catalog.title} subtitle={dashboardLabels.catalog.subtitle} />
      <CatalogView />
    </>
  );
}
