import { DatePicker, Form, Input, Select } from 'antd';
import { useDictionary } from '@/features/dictionaries';
import { MilitaryUnitSelect, SubdivisionTreeSelect, useRegions, useTerritorialDistricts } from '@/features/organization';
import { DISPLAY_DATE_FORMAT } from '@/lib/dayjs';
import { common } from '@/lib/i18n';
import { PASSPORT_PATTERN, PHONE_PATTERN } from '@/utils/validators';
import { soldierLabels } from '../labels';
import type { FieldSource } from '../types';
import { FieldSourceTag } from './FieldSourceTag';

type GeneralInfoSectionProps = {
  sourceOf: (field: string) => FieldSource | undefined;
};

const required = [{ required: true, message: common.fields.required }];

export function GeneralInfoSection({ sourceOf }: GeneralInfoSectionProps) {
  const form = Form.useFormInstance();
  const regionId = Form.useWatch('regionId', form) as number | undefined;
  const unitId = Form.useWatch('militaryUnitId', form) as number | undefined;
  const { data: regions = [] } = useRegions();
  const { data: districts = [] } = useTerritorialDistricts(regionId);
  const { data: kinships = [] } = useDictionary('KINSHIP');
  const f = soldierLabels.fields;
  const label = (text: string, field: string) => <>{text}<FieldSourceTag source={sourceOf(field)} /></>;

  return (
    <>
      <Form.Item name="pinfl" label={f.pinfl}><Input disabled /></Form.Item>
      <Form.Item name="fullName" label={label(common.fields.fullName, 'fullName')} rules={required}><Input /></Form.Item>
      <Form.Item name="birthDate" label={label(f.birthDate, 'birthDate')} rules={required}>
        <DatePicker format={DISPLAY_DATE_FORMAT} style={{ width: '100%' }} />
      </Form.Item>
      <Form.Item
        name="passport"
        label={label(f.passport, 'passport')}
        extra={f.passportHint}
        rules={[...required, { pattern: PASSPORT_PATTERN, message: f.passportHint }]}
        normalize={(value?: string) => value?.toUpperCase()}
      >
        <Input maxLength={9} />
      </Form.Item>
      <Form.Item name="phone" label={f.phone} extra={f.phoneHint} rules={[...required, { pattern: PHONE_PATTERN, message: f.phoneHint }]}>
        <Input maxLength={13} inputMode="tel" />
      </Form.Item>
      <Form.Item name="phoneKinshipId" label={f.kinship} rules={required}>
        <Select options={kinships.map((item) => ({ value: item.id, label: item.name }))} />
      </Form.Item>
      <Form.Item name="regionId" label={label(f.region, 'regionId')} rules={required}>
        <Select
          showSearch
          optionFilterProp="label"
          options={regions.map((region) => ({ value: region.id, label: region.name }))}
          onChange={() => form.setFieldValue('districtId', undefined)}
        />
      </Form.Item>
      <Form.Item name="districtId" label={label(f.district, 'districtId')} rules={required}>
        <Select
          showSearch
          optionFilterProp="label"
          disabled={regionId === undefined}
          options={districts.map((district) => ({ value: district.id, label: district.name }))}
        />
      </Form.Item>
      <Form.Item name="mahalla" label={label(f.mahalla, 'mahalla')} rules={required}><Input /></Form.Item>
      <Form.Item name="street" label={label(f.street, 'street')} rules={required}><Input /></Form.Item>
      <Form.Item name="house" label={label(f.house, 'house')} rules={required}><Input /></Form.Item>
      <Form.Item name="apartment" label={f.apartment}><Input /></Form.Item>
      <Form.Item name="militaryUnitId" label={f.unit} rules={required}>
        <MilitaryUnitSelect style={{ width: '100%' }} onChange={() => form.setFieldValue('subdivisionId', undefined)} />
      </Form.Item>
      <Form.Item name="subdivisionId" label={f.subdivision}>
        <SubdivisionTreeSelect unitId={unitId} style={{ width: '100%' }} />
      </Form.Item>
      <Form.Item name="conscriptionDate" label={f.conscriptionDate}>
        <DatePicker format={DISPLAY_DATE_FORMAT} style={{ width: '100%' }} />
      </Form.Item>
      <Form.Item name="serviceEndDate" label={f.serviceEndDate}>
        <DatePicker format={DISPLAY_DATE_FORMAT} style={{ width: '100%' }} />
      </Form.Item>
    </>
  );
}
