import { can, type Capability } from '../permissions';
import { useAuth } from './useAuth';

export function useCan(capability: Capability): boolean {
  const { user } = useAuth();
  return can(user?.role, capability);
}
