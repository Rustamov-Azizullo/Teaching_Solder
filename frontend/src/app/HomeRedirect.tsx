import { Navigate } from 'react-router-dom';
import { can, useAuth } from '@/features/auth';

/** Ruxsatlarga qarab birinchi ish sahifasiga yo'naltiradi. */
export function HomeRedirect() {
  const { user } = useAuth();
  if (can(user, 'dashboardVocational') || can(user, 'dashboardOtm')) return <Navigate to="/dashboard" replace />;
  if (can(user, 'soldierRead')) return <Navigate to="/soldiers" replace />;
  return <Navigate to="/groups" replace />;
}
