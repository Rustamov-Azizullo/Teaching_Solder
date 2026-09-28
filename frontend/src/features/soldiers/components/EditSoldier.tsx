import { useNavigate } from 'react-router-dom';
import { QueryBoundary } from '@/components/ui';
import { useSoldier } from '../hooks/useSoldiers';
import { SoldierForm } from './SoldierForm';

export function EditSoldier({ soldierId }: { soldierId: number }) {
  const navigate = useNavigate();
  const { data, isLoading, error, refetch } = useSoldier(soldierId);
  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {(soldier) => (
        <SoldierForm
          pinfl={soldier.pinfl}
          soldier={soldier}
          onSaved={() => navigate(`/soldiers/${soldierId}`)}
          onCancel={() => navigate(`/soldiers/${soldierId}`)}
        />
      )}
    </QueryBoundary>
  );
}
