import { Card, Descriptions, Space, Tag } from 'antd';
import { formatDate } from '@/utils/format';
import {
  awardPlaceLabels,
  generalEducationLabels,
  higherEducationLabels,
  professionalEducationLabels,
  soldierLabels,
} from '../labels';
import type { Certificate, Soldier } from '../types';
import { FieldSourceTag } from './FieldSourceTag';

const CERTIFICATE_TITLES = {
  LANGUAGE: soldierLabels.fields.languageCertificates,
  PROFESSION: soldierLabels.fields.professionCertificates,
  SUBJECT: soldierLabels.fields.subjectCertificates,
} as const;

function certificateText(certificate: Certificate): string {
  const name = certificate.dictionaryItemName ?? certificate.title ?? '';
  return certificate.level ? `${name} (${certificate.level})` : name;
}

export function SoldierProfile({ soldier }: { soldier: Soldier }) {
  const f = soldierLabels.fields;
  const sourceOf = (field: string) => soldier.fieldSources[field]?.source;
  const address = [soldier.regionName, soldier.districtName, soldier.mahalla, soldier.street, soldier.house, soldier.apartment]
    .filter(Boolean)
    .join(', ');

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Card title={soldierLabels.sections.general}>
        <Descriptions column={{ xs: 1, md: 2 }} size="small" bordered>
          <Descriptions.Item label={f.pinfl}>{soldier.pinfl}</Descriptions.Item>
          <Descriptions.Item label="F.I.Sh.">{soldier.fullName}<FieldSourceTag source={sourceOf('fullName')} /></Descriptions.Item>
          <Descriptions.Item label={f.birthDate}>{formatDate(soldier.birthDate)}<FieldSourceTag source={sourceOf('birthDate')} /></Descriptions.Item>
          <Descriptions.Item label={f.passport}>{soldier.passport}<FieldSourceTag source={sourceOf('passport')} /></Descriptions.Item>
          <Descriptions.Item label={f.phone}>{soldier.phone} ({soldier.phoneKinshipName})</Descriptions.Item>
          <Descriptions.Item label={f.unit}>{soldier.militaryUnitName}</Descriptions.Item>
          <Descriptions.Item label={f.region} span={2}>{address}</Descriptions.Item>
          <Descriptions.Item label={f.conscriptionDate}>{formatDate(soldier.conscriptionDate)}</Descriptions.Item>
          <Descriptions.Item label={f.serviceEndDate}>{formatDate(soldier.serviceEndDate)}</Descriptions.Item>
        </Descriptions>
      </Card>
      <Card title={soldierLabels.sections.education}>
        <Descriptions column={{ xs: 1, md: 3 }} size="small" bordered>
          <Descriptions.Item label={f.generalEducation}>{generalEducationLabels[soldier.generalEducation]}</Descriptions.Item>
          <Descriptions.Item label={f.professionalEducation}>
            {soldier.professionalEducation ? professionalEducationLabels[soldier.professionalEducation] : '—'}
          </Descriptions.Item>
          <Descriptions.Item label={f.higherEducation}>
            {soldier.higherEducation ? higherEducationLabels[soldier.higherEducation] : '—'}
          </Descriptions.Item>
        </Descriptions>
      </Card>
      <Card title={soldierLabels.sections.skills}>
        <Descriptions column={1} size="small" bordered>
          <Descriptions.Item label={f.priorOccupation}>
            {soldier.noPriorOccupation ? f.noPriorOccupation : soldier.priorOccupation ?? '—'}
          </Descriptions.Item>
          <Descriptions.Item label={f.awards}>
            {soldier.awards.length === 0 ? '—' : soldier.awards.map((award) => (
              <Tag key={award.id}>{award.kindName}: {awardPlaceLabels[award.place]}</Tag>
            ))}
          </Descriptions.Item>
          {(Object.keys(CERTIFICATE_TITLES) as (keyof typeof CERTIFICATE_TITLES)[]).map((kind) => {
            const items = soldier.certificates.filter((certificate) => certificate.kind === kind);
            return (
              <Descriptions.Item key={kind} label={CERTIFICATE_TITLES[kind]}>
                {items.length === 0 ? '—' : items.map((item) => <Tag key={item.id}>{certificateText(item)}</Tag>)}
              </Descriptions.Item>
            );
          })}
        </Descriptions>
      </Card>
    </Space>
  );
}
