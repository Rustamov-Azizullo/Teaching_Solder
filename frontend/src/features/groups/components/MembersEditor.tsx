import { Alert, Button, Space, Transfer } from 'antd';
import { useEffect, useState } from 'react';
import { useSoldierSearch } from '@/features/soldiers';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { useReplaceMembers, useSoldiersInOtherGroups } from '../hooks/useGroups';
import { groupLabels } from '../labels';
import type { Group } from '../types';
import { notify } from '@/lib/notify';

const MAX_UNIT_SOLDIERS = 100;

export function MembersEditor({ group, canEdit }: { group: Group; canEdit: boolean }) {
  const { data } = useSoldierSearch({ unitId: group.militaryUnitId, page: 0, size: MAX_UNIT_SOLDIERS });
  const { data: takenSoldierIds } = useSoldiersInOtherGroups(group.id);
  const { mutateAsync, isPending } = useReplaceMembers(group.id);
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);

  useEffect(() => setSelectedKeys(group.members.map((member) => String(member.id))), [group.members]);

  const candidates = new Map<string, string>();
  const takenIds = new Set(takenSoldierIds);
  data?.content
    .filter((soldier) => !takenIds.has(soldier.id))
    .forEach((soldier) => candidates.set(String(soldier.id), soldier.fullName));
  group.members.forEach((member) => candidates.set(String(member.id), member.fullName));

  const handleSave = async () => {
    try {
      await mutateAsync(selectedKeys.map(Number));
      notify.success(groupLabels.saved);
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Alert type="info" showIcon message={groupLabels.membersHint} />
      <Transfer
        showSearch
        disabled={!canEdit}
        listStyle={{ width: 320, height: 380 }}
        dataSource={[...candidates].map(([key, title]) => ({ key, title }))}
        targetKeys={selectedKeys}
        onChange={(keys) => setSelectedKeys(keys as string[])}
        render={(item) => item.title}
        filterOption={(input, item) => item.title.toLowerCase().includes(input.toLowerCase())}
      />
      {canEdit && <Button type="primary" loading={isPending} onClick={handleSave}>{common.actions.save}</Button>}
    </Space>
  );
}
