import type { PropsWithChildren } from 'react';
import { ErrorState } from '@/components/ui/ErrorState';
import { common } from '@/lib/i18n';
import { canAny, type Capability } from '../permissions';
import { useAuth } from '../hooks/useAuth';

/** `capability` massiv bo'lsa, ulardan bittasi yetarli. */
type RequireCapabilityProps = PropsWithChildren<{ capability: Capability | readonly Capability[] }>;

export function RequireCapability({ capability, children }: RequireCapabilityProps) {
  const { user } = useAuth();
  const isAllowed = canAny(user, Array.isArray(capability) ? capability : [capability as Capability]);
  if (!isAllowed) return <ErrorState message={common.states.noAccess} />;
  return <>{children}</>;
}
