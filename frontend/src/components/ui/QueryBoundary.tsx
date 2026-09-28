import { Empty } from 'antd';
import type { ReactNode } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { ErrorState } from './ErrorState';
import { FullPageSpinner } from './FullPageSpinner';

type QueryBoundaryProps<T> = {
  isLoading: boolean;
  error: unknown;
  data: T | undefined;
  onRetry?: () => void;
  isEmpty?: (data: T) => boolean;
  children: (data: T) => ReactNode;
};

/** Ma'lumot yuklashning uch holatini (loading / error / empty) bir joyda qamrab oladi. */
export function QueryBoundary<T>({ isLoading, error, data, onRetry, isEmpty, children }: QueryBoundaryProps<T>) {
  if (isLoading) return <FullPageSpinner />;
  if (error) return <ErrorState message={getErrorMessage(error)} onRetry={onRetry} />;
  if (data === undefined) return null;
  if (isEmpty?.(data)) return <Empty description={common.states.empty} />;
  return <>{children(data)}</>;
}
