import { useParams } from 'react-router-dom';

/** Marshrut parametridan raqamli id ni oladi. */
export function useIdParam(name: string): number {
  const params = useParams();
  return Number(params[name]);
}
