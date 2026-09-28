import { PageHeader } from '@/components/ui';
import { TwoFactorPanel, authLabels } from '@/features/auth';

export function ProfilePage() {
  return (
    <>
      <PageHeader title={authLabels.profileTitle} />
      <TwoFactorPanel />
    </>
  );
}
