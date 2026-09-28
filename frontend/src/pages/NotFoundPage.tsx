import { Button, Result } from 'antd';
import { Link } from 'react-router-dom';
import { common } from '@/lib/i18n';

export function NotFoundPage() {
  return (
    <Result
      status="404"
      title={common.states.notFound}
      extra={<Link to="/"><Button type="primary">{common.actions.back}</Button></Link>}
    />
  );
}
