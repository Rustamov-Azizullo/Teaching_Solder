import { SoldierDetails } from '@/features/soldiers';
import { useIdParam } from '@/hooks/useIdParam';

export function SoldierDetailPage() {
  return <SoldierDetails soldierId={useIdParam('soldierId')} />;
}
