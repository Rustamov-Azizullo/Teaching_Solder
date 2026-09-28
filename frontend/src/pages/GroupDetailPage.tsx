import { GroupDetail } from '@/features/groups';
import { useIdParam } from '@/hooks/useIdParam';

export function GroupDetailPage() {
  return <GroupDetail groupId={useIdParam('groupId')} />;
}
