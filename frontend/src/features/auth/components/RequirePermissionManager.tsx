import type { PropsWithChildren } from 'react';
import { ErrorState } from '@/components/ui/ErrorState';
import { common } from '@/lib/i18n';
import { useAuth } from '../hooks/useAuth';
import { isPermissionManager } from '../permissions';

/** Faqat SuperAdmin / Mega SuperAdmin uchun (backenddagi statik `PERMISSION_MANAGE` tekshiruvi bilan mos). */
export function RequirePermissionManager({ children }: PropsWithChildren) {
  const { user } = useAuth();
  if (!isPermissionManager(user)) return <ErrorState message={common.states.noAccess} />;
  return <>{children}</>;
}
