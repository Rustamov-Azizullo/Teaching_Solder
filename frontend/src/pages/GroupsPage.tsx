import { Collapse } from 'antd';
import { PageHeader } from '@/components/ui';
import { useCan } from '@/features/auth';
import { GroupList, groupLabels } from '@/features/groups';
import { GroupSuggestions, surveyLabels } from '@/features/surveys';

export function GroupsPage() {
  const canCreate = useCan('groupWrite');
  return (
    <>
      <PageHeader title={groupLabels.listTitle} subtitle={groupLabels.listSubtitle} />
      <GroupList />
      {canCreate && (
        <Collapse style={{ marginTop: 24 }} items={[{ key: 's', label: surveyLabels.suggestions.title, children: <GroupSuggestions /> }]} />
      )}
    </>
  );
}
