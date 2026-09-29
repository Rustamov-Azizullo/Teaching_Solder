import { Alert, Button, Select, Space } from 'antd';
import { useEffect, useState } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { useInstitutions, useReplaceTeachers, useTeachers } from '../hooks/useGroups';
import { groupLabels } from '../labels';
import type { Group } from '../types';
import { notify } from '@/lib/notify';

export function GroupTeachersEditor({ group, canEdit }: { group: Group; canEdit: boolean }) {
  const { data: teachers = [] } = useTeachers();
  const { data: contractedInstitutions = [] } = useInstitutions(group.militaryUnitId);
  const { mutateAsync, isPending } = useReplaceTeachers(group.id);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  useEffect(() => setSelectedIds(group.teachers.map((teacher) => teacher.id)), [group.teachers]);

  // Faqat guruh harbiy qismi bilan shartnomasi bor muassasalarning o'qituvchilari.
  const unitTeachers = teachers.filter((teacher) => contractedInstitutions.some((institution) => institution.id === teacher.institutionId));

  const handleSave = async () => {
    try {
      await mutateAsync(selectedIds);
      notify.success(groupLabels.saved);
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%', maxWidth: 560 }}>
      <Alert type="info" showIcon message={groupLabels.teachersHint} />
      <Select
        mode="multiple"
        disabled={!canEdit}
        style={{ width: '100%' }}
        value={selectedIds}
        onChange={setSelectedIds}
        optionFilterProp="label"
        options={unitTeachers.map((teacher) => ({ value: teacher.id, label: `${teacher.fullName} — ${teacher.specialty}` }))}
      />
      {canEdit && <Button type="primary" loading={isPending} onClick={handleSave}>{common.actions.save}</Button>}
    </Space>
  );
}
