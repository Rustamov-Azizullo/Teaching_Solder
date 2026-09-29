import { Col, DatePicker, Divider, Form, Input, Modal, Row, Select, Typography } from 'antd';
import { useEffect } from 'react';
import type { Dayjs } from 'dayjs';
import { useAuth, useCan } from '@/features/auth';
import { useDictionary } from '@/features/dictionaries';
import { MilitaryUnitSelect } from '@/features/organization';
import { getErrorMessage } from '@/lib/apiClient';
import { API_DATE_FORMAT, DISPLAY_DATE_FORMAT, dayjs } from '@/lib/dayjs';
import { common } from '@/lib/i18n';
import { useCreateGroup, useInstitutions, useUpdateGroup } from '../hooks/useGroups';
import { groupLabels, groupTypeLabels } from '../labels';
import type { Group, GroupType } from '../types';
import { notify } from '@/lib/notify';

type FormValues = {
  name: string;
  type: GroupType;
  militaryUnitId: number;
  institutionId?: number;
  professionId?: number;
  subjectIds?: number[];
  period: [Dayjs, Dayjs];
  classroom?: string;
  leaderFullName?: string;
  leaderPinfl?: string;
  leaderRank?: string;
  leaderPhone?: string;
};

type GroupFormModalProps = { open: boolean; group?: Group; onClose: () => void };

const PINFL_PATTERN = /^\d{14}$/;
const PINFL_LENGTH = 14;
const MODAL_WIDTH = 760;
const required = [{ required: true, message: common.fields.required }];

export function GroupFormModal({ open, group, onClose }: GroupFormModalProps) {
  const { user } = useAuth();
  const canSetLeader = useCan('leaderAssign');
  const [form] = Form.useForm<FormValues>();
  const type = Form.useWatch('type', form) ?? group?.type;
  const create = useCreateGroup();
  const update = useUpdateGroup(group?.id ?? 0);
  const { data: professions = [] } = useDictionary('PROFESSION');
  const { data: subjects = [] } = useDictionary('SUBJECT');
  const unitId = Form.useWatch('militaryUnitId', form);
  const { data: institutions = [], isLoading: isInstitutionsLoading } = useInstitutions(unitId, unitId !== undefined);

  useEffect(() => {
    if (!open) return;
    form.resetFields();
    form.setFieldsValue(
      group
        ? {
            name: group.name, type: group.type, militaryUnitId: group.militaryUnitId,
            institutionId: group.institution?.id, professionId: group.profession?.id,
            subjectIds: group.subjects.map((subject) => subject.id),
            period: [dayjs(group.startDate), dayjs(group.endDate)], classroom: group.classroom ?? undefined,
            leaderFullName: group.leader?.fullName, leaderPinfl: group.leader?.pinfl ?? undefined,
            leaderRank: group.leader?.militaryRank ?? undefined, leaderPhone: group.leader?.phone ?? undefined,
          }
        : { type: 'VOCATIONAL', militaryUnitId: user?.militaryUnitId ?? undefined },
    );
  }, [open, group, form, user]);

  const handleOk = async () => {
    const { period, leaderFullName, leaderPinfl, leaderRank, leaderPhone, ...values } = await form.validateFields();
    const request = {
      ...values,
      leader: canSetLeader && leaderFullName && leaderPinfl
        ? { fullName: leaderFullName, pinfl: leaderPinfl, militaryRank: leaderRank, phone: leaderPhone }
        : undefined,
      subjectIds: values.type === 'OTM_PREP' ? values.subjectIds : undefined,
      professionId: values.type === 'VOCATIONAL' ? values.professionId : undefined,
      startDate: period[0].format(API_DATE_FORMAT),
      endDate: period[1].format(API_DATE_FORMAT),
    };
    try {
      await (group ? update.mutateAsync(request) : create.mutateAsync(request));
      notify.success(groupLabels.saved);
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  const f = groupLabels.fields;
  const lf = groupLabels.leaderForm;

  /** Guruh kattasi ism va JShShIR bilan birga to'ldiriladi: bittasi yozilsa, ikkinchisi ham majburiy. */
  const leaderRule = (otherField: keyof FormValues) => ({
    validator: (_: unknown, value?: string) =>
      !value && form.getFieldValue(otherField) ? Promise.reject(new Error(common.fields.required)) : Promise.resolve(),
  });

  return (
    <Modal
      open={open}
      title={group ? groupLabels.editTitle : groupLabels.createTitle}
      onOk={handleOk}
      onCancel={onClose}
      confirmLoading={create.isPending || update.isPending}
      okText={common.actions.save}
      cancelText={common.actions.cancel}
      width={MODAL_WIDTH}
      destroyOnHidden
    >
      <Form form={form} layout="vertical" onValuesChange={(changed) => { if ('militaryUnitId' in changed) form.setFieldsValue({ institutionId: undefined }); }}>
        <Row gutter={16}>
          <Col xs={24} md={12}>
            <Form.Item name="name" label={f.name} rules={required}><Input /></Form.Item>
          </Col>
          <Col xs={24} md={12}>
            <Form.Item name="type" label={f.type} rules={required}>
              <Select
                disabled={group !== undefined}
                options={Object.entries(groupTypeLabels).map(([value, label]) => ({ value, label }))}
              />
            </Form.Item>
          </Col>
          <Col xs={24} md={12}>
            <Form.Item name="militaryUnitId" label={f.unit} rules={required}>
              <MilitaryUnitSelect style={{ width: '100%' }} />
            </Form.Item>
          </Col>
          <Col xs={24} md={12}>
            <Form.Item name="institutionId" label={f.institution}
              extra={unitId !== undefined && !isInstitutionsLoading && institutions.length === 0 ? groupLabels.institutionNoContract : undefined}>
              <Select allowClear showSearch optionFilterProp="label" disabled={unitId === undefined} loading={isInstitutionsLoading}
                placeholder={unitId === undefined ? groupLabels.institutionPickUnit : undefined}
                options={institutions.map((item) => ({ value: item.id, label: item.name }))} />
            </Form.Item>
          </Col>
          <Col xs={24} md={12}>
            {type === 'VOCATIONAL' ? (
              <Form.Item name="professionId" label={f.profession} rules={required} extra={groupLabels.vocationalNote}>
                <Select options={professions.map((item) => ({ value: item.id, label: item.name }))} />
              </Form.Item>
            ) : (
              <Form.Item name="subjectIds" label={f.subjects} rules={required}>
                <Select mode="multiple" options={subjects.map((item) => ({ value: item.id, label: item.name }))} />
              </Form.Item>
            )}
          </Col>
          <Col xs={24} md={12}>
            <Form.Item name="period" label={`${f.startDate} — ${f.endDate}`} rules={required}>
              <DatePicker.RangePicker format={DISPLAY_DATE_FORMAT} style={{ width: '100%' }} />
            </Form.Item>
          </Col>
          <Col xs={24} md={12}>
            <Form.Item name="classroom" label={f.classroom}><Input /></Form.Item>
          </Col>
        </Row>
        {canSetLeader && (
          <>
            <Divider orientation="left">{lf.section}</Divider>
            <Typography.Paragraph type="secondary">{lf.hint}</Typography.Paragraph>
            <Row gutter={16}>
              <Col xs={24} md={12}>
                <Form.Item name="leaderFullName" label={common.fields.fullName} dependencies={['leaderPinfl']} rules={[leaderRule('leaderPinfl')]}>
                  <Input />
                </Form.Item>
              </Col>
              <Col xs={24} md={12}>
                <Form.Item
                  name="leaderPinfl"
                  label={lf.pinfl}
                  dependencies={['leaderFullName']}
                  rules={[leaderRule('leaderFullName'), { pattern: PINFL_PATTERN, message: lf.pinflInvalid }]}
                >
                  <Input maxLength={PINFL_LENGTH} inputMode="numeric" />
                </Form.Item>
              </Col>
              <Col xs={24} md={12}>
                <Form.Item name="leaderRank" label={lf.rank}><Input /></Form.Item>
              </Col>
              <Col xs={24} md={12}>
                <Form.Item name="leaderPhone" label={lf.phone}><Input /></Form.Item>
              </Col>
            </Row>
          </>
        )}
      </Form>
    </Modal>
  );
}
