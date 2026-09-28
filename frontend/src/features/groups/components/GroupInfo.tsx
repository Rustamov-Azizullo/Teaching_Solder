import { UserSwitchOutlined } from '@ant-design/icons';
import { Button, Descriptions, Select } from 'antd';
import { useCan } from '@/features/auth';
import { useFacilities } from '@/features/facilities';
import { getErrorMessage } from '@/lib/apiClient';
import { notify } from '@/lib/notify';
import { formatDate } from '@/utils/format';
import { useAssignFacility } from '../hooks/useGroups';
import { groupLabels, groupTypeLabels } from '../labels';
import type { Group } from '../types';

type GroupInfoProps = { group: Group; canAssignLeader: boolean; onAssignLeader: () => void };

export function GroupInfo({ group, canAssignLeader, onAssignLeader }: GroupInfoProps) {
  const f = groupLabels.fields;
  const canAssignFacility = useCan('leaderAssign');
  const { data: facilities = [] } = useFacilities();
  const assign = useAssignFacility(group.id);
  const unitFacilities = facilities.filter((facility) => facility.militaryUnitId === group.militaryUnitId);

  const handleFacility = async (facilityId: number) => {
    try {
      await assign.mutateAsync(facilityId);
      notify.success(groupLabels.saved);
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };
  return (
    <Descriptions bordered size="small" column={{ xs: 1, md: 2 }}>
      <Descriptions.Item label={f.type}>{groupTypeLabels[group.type]}</Descriptions.Item>
      <Descriptions.Item label={f.unit}>{group.militaryUnitName}</Descriptions.Item>
      <Descriptions.Item label={f.institution}>{group.institution?.name ?? '—'}</Descriptions.Item>
      <Descriptions.Item label={group.profession ? f.profession : f.subjects}>
        {group.profession?.name ?? group.subjects.map((subject) => subject.name).join(', ')}
      </Descriptions.Item>
      <Descriptions.Item label={f.startDate}>{formatDate(group.startDate)}</Descriptions.Item>
      <Descriptions.Item label={f.endDate}>{formatDate(group.endDate)}</Descriptions.Item>
      <Descriptions.Item label={f.classroom}>
        {group.classroom ?? '—'}
        {canAssignFacility && (
          <Select
            size="small"
            placeholder={f.facility}
            style={{ minWidth: 200, marginInlineStart: 12 }}
            loading={assign.isPending}
            onChange={handleFacility}
            options={unitFacilities.map((facility) => ({ value: facility.id, label: facility.name }))}
          />
        )}
      </Descriptions.Item>
      <Descriptions.Item label={f.leader}>
        {group.leader ? `${group.leader.name} (${f.leaderOrder} ${group.leaderOrderNo}, ${formatDate(group.leaderOrderDate)})` : groupLabels.noLeader}
        {canAssignLeader && (
          <Button type="link" icon={<UserSwitchOutlined />} onClick={onAssignLeader}>{groupLabels.assignLeader}</Button>
        )}
      </Descriptions.Item>
    </Descriptions>
  );
}
