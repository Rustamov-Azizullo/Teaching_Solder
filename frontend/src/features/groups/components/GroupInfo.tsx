import { DeleteOutlined } from '@ant-design/icons';
import { Button, Descriptions, Popconfirm } from 'antd';
import { common } from '@/lib/i18n';
import { formatDate } from '@/utils/format';
import { groupLabels, groupTypeLabels } from '../labels';
import type { Group, GroupLeaderInfo } from '../types';

function describeLeader(leader: GroupLeaderInfo): string {
  return [leader.fullName, leader.militaryRank, leader.pinfl, leader.phone].filter(Boolean).join(', ');
}

type GroupInfoProps = { group: Group; canAssignLeader: boolean; onRemoveLeader: () => void };

export function GroupInfo({ group, canAssignLeader, onRemoveLeader }: GroupInfoProps) {
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
        {group.leader ? describeLeader(group.leader) : groupLabels.noLeader}
        {canAssignLeader && group.leader && (
          <Popconfirm title={groupLabels.removeLeader + '?'} okText={common.actions.delete} cancelText={common.actions.cancel} onConfirm={onRemoveLeader}>
            <Button type="link" danger icon={<DeleteOutlined />}>{groupLabels.removeLeader}</Button>
          </Popconfirm>
        )}
      </Descriptions.Item>
    </Descriptions>
  );
}
