import { PageHeader } from '@/components/ui';
import { KtaComparison, resultLabels } from '@/features/results';

export function KtaPage() {
  return (
    <>
      <PageHeader title={resultLabels.kta.title} subtitle={resultLabels.kta.subtitle} />
      <KtaComparison />
    </>
  );
}
