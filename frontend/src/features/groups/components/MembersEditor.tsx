import { Alert, Button, Space, Transfer } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { SubdivisionTreeSelect } from '@/features/organization';
import { useSoldierSearch } from '@/features/soldiers';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { useReplaceMembers, useSoldiersInOtherGroups } from '../hooks/useGroups';
import { groupLabels } from '../labels';
import type { Group } from '../types';
import { notify } from '@/lib/notify';

const MAX_UNIT_SOLDIERS = 100;

export function MembersEditor({ group, canEdit }: { group: Group; canEdit: boolean }) {
  const [subdivisionId, setSubdivisionId] = useState<number>();
  const { data } = useSoldierSearch({ unitId: group.militaryUnitId, subdivisionId, page: 0, size: MAX_UNIT_SOLDIERS });
  const { data: takenSoldierIds } = useSoldiersInOtherGroups(group.id);
  const { mutateAsync, isPending } = useReplaceMembers(group.id);
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);

  useEffect(() => setSelectedKeys(group.members.map((member) => String(member.id))), [group.members]);

  // Bo'linma almashtirilganda tanlab qo'yilgan askarlar ro'yxatdan yo'qolmasligi uchun nomlar yig'ib boriladi.
  const [knownNames, setKnownNames] = useState<Map<string, string>>(new Map());
  const takenIds = useMemo(() => new Set(takenSoldierIds), [takenSoldierIds]);

  useEffect(() => {
    setKnownNames((previous) => {
      const next = new Map(previous);
      group.members.forEach((member) => next.set(String(member.id), member.fullName));
      data?.content.filter((soldier) => !takenIds.has(soldier.id))
        .forEach((soldier) => next.set(String(soldier.id), soldier.fullName));
      return next;
    });
  }, [data, takenIds, group.members]);

  const visibleKeys = new Set<string>([
    ...selectedKeys,
    ...(data?.content.filter((soldier) => !takenIds.has(soldier.id)).map((soldier) => String(soldier.id)) ?? []),
  ]);
  const candidates = [...knownNames].filter(([key]) => visibleKeys.has(key));

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
      <SubdivisionTreeSelect unitId={group.militaryUnitId} value={subdivisionId} onChange={setSubdivisionId}
        placeholder={groupLabels.membersSubdivision} style={{ width: 320 }} />
      <Transfer
        showSearch
        disabled={!canEdit}
        listStyle={{ width: 320, height: 380 }}
        dataSource={candidates.map(([key, title]) => ({ key, title }))}
        targetKeys={selectedKeys}
        onChange={(keys) => setSelectedKeys(keys as string[])}
        render={(item) => item.title}
        filterOption={(input, item) => item.title.toLowerCase().includes(input.toLowerCase())}
      />
      {canEdit && <Button type="primary" loading={isPending} onClick={handleSave}>{common.actions.save}</Button>}
    </Space>
  );
}
