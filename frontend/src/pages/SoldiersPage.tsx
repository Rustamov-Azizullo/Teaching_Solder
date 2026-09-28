import { PageHeader } from '@/components/ui';
import { SoldierList, soldierLabels } from '@/features/soldiers';

export function SoldiersPage() {
  return (
    <>
      <PageHeader title={soldierLabels.listTitle} subtitle={soldierLabels.listSubtitle} />
      <SoldierList />
    </>
  );
}
