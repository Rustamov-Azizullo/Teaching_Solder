import type { PropsWithChildren } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { canOpenScopedCatalog } from '../permissions';

/** Faqat Admin/User uchun; boshqa rollar (masalan, SuperAdmin) bosh sahifaga qaytariladi. */
export function RequireScopedCatalog({ children }: PropsWithChildren) {
  const { user } = useAuth();
  if (!canOpenScopedCatalog(user)) return <Navigate to="/" replace />;
  return <>{children}</>;
}
