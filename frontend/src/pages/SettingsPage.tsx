import { PageHeader } from '@/components/ui';
import { CyclesPanel, RetentionPanel, SettingsForm, adminLabels } from '@/features/admin';

export function SettingsPage() {
  return (
    <>
      <PageHeader title={adminLabels.settings.title} subtitle={adminLabels.settings.subtitle} />
      <CyclesPanel />
      <RetentionPanel />
      <SettingsForm />
    </>
  );
}
