import { Alert, Button } from 'antd';
import { common } from '@/lib/i18n';

type ErrorStateProps = { message?: string; onRetry?: () => void };

export function ErrorState({ message = common.states.error, onRetry }: ErrorStateProps) {
  return (
    <Alert
      type="error"
      showIcon
      message={message}
      action={onRetry && <Button size="small" onClick={onRetry}>{common.actions.refresh}</Button>}
    />
  );
}
