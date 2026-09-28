import { PageHeader } from '@/components/ui';
import { DictionaryManager, dictionaryLabels } from '@/features/dictionaries';

export function DictionariesPage() {
  return (
    <>
      <PageHeader title={dictionaryLabels.title} subtitle={dictionaryLabels.subtitle} />
      <DictionaryManager />
    </>
  );
}
