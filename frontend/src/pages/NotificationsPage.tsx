import { PageHeader } from '@/components/ui';
import { NotificationList, notificationLabels } from '@/features/notifications';

export function NotificationsPage() {
  return (
    <>
      <PageHeader title={notificationLabels.title} subtitle={notificationLabels.subtitle} />
      <NotificationList />
    </>
  );
}
