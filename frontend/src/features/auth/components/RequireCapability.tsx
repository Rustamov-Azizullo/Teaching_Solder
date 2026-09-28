import type { PropsWithChildren } from 'react';
import { ErrorState } from '@/components/ui/ErrorState';
import { common } from '@/lib/i18n';
import { useCan } from '../hooks/useCan';
import type { Capability } from '../permissions';

type RequireCapabilityProps = PropsWithChildren<{ capability: Capability }>;

export function RequireCapability({ capability, children }: RequireCapabilityProps) {
  const isAllowed = useCan(capability);
  if (!isAllowed) return <ErrorState message={common.states.noAccess} />;
  return <>{children}</>;
}
