import { UserSwitchOutlined } from '@ant-design/icons';
import { Button, Descriptions } from 'antd';
import { formatDate } from '@/utils/format';
import { groupLabels, groupTypeLabels } from '../labels';
import type { Group } from '../types';

type GroupInfoProps = { group: Group; canAssignLeader: boolean; onAssignLeader: () => void };

export function GroupInfo({ group, canAssignLeader, onAssignLeader }: GroupInfoProps) {
  const f = groupLabels.fields;
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
      <Descriptions.Item label={f.classroom}>{group.classroom ?? '—'}</Descriptions.Item>
      <Descriptions.Item label={f.leader}>
        {group.leader ? `${group.leader.name} (${f.leaderOrder} ${group.leaderOrderNo}, ${formatDate(group.leaderOrderDate)})` : groupLabels.noLeader}
        {canAssignLeader && (
          <Button type="link" icon={<UserSwitchOutlined />} onClick={onAssignLeader}>{groupLabels.assignLeader}</Button>
        )}
      </Descriptions.Item>
    </Descriptions>
  );
}
