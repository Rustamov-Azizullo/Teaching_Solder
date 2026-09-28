import { PageHeader } from '@/components/ui';
import { EditSoldier, soldierLabels } from '@/features/soldiers';
import { useIdParam } from '@/hooks/useIdParam';

export function SoldierEditPage() {
  return (
    <>
      <PageHeader title={soldierLabels.editTitle} />
      <EditSoldier soldierId={useIdParam('soldierId')} />
    </>
  );
}
