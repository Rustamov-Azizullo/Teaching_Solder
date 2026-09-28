import { Navigate } from 'react-router-dom';
import { can, useAuth } from '@/features/auth';

/** Rolga qarab birinchi ish sahifasiga yo'naltiradi. */
export function HomeRedirect() {
  const { user } = useAuth();
  const role = user?.role;
  if (can(role, 'dashboardVocational') || can(role, 'dashboardOtm')) return <Navigate to="/dashboard" replace />;
  if (can(role, 'soldierRead')) return <Navigate to="/soldiers" replace />;
  return <Navigate to="/groups" replace />;
}
