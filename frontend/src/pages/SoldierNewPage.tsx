import { PageHeader } from '@/components/ui';
import { NewSoldierFlow, soldierLabels } from '@/features/soldiers';

export function SoldierNewPage() {
  return (
    <>
      <PageHeader title={soldierLabels.createTitle} />
      <NewSoldierFlow />
    </>
  );
}
