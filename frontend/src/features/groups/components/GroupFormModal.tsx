import { DatePicker, Form, Input, Modal, Select } from 'antd';
import { useEffect } from 'react';
import type { Dayjs } from 'dayjs';
import { useAuth } from '@/features/auth';
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
};

type GroupFormModalProps = { open: boolean; group?: Group; onClose: () => void };

const required = [{ required: true, message: common.fields.required }];

export function GroupFormModal({ open, group, onClose }: GroupFormModalProps) {
  const { user } = useAuth();
  const [form] = Form.useForm<FormValues>();
  const type = Form.useWatch('type', form) ?? group?.type;
  const create = useCreateGroup();
  const update = useUpdateGroup(group?.id ?? 0);
  const { data: professions = [] } = useDictionary('PROFESSION');
  const { data: subjects = [] } = useDictionary('SUBJECT');
  const { data: institutions = [] } = useInstitutions();

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
          }
        : { type: 'VOCATIONAL', militaryUnitId: user?.militaryUnitId ?? undefined },
    );
  }, [open, group, form, user]);

  const handleOk = async () => {
    const { period, ...values } = await form.validateFields();
    const request = {
      ...values,
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
  return (
    <Modal
      open={open}
      title={group ? groupLabels.editTitle : groupLabels.createTitle}
      onOk={handleOk}
      onCancel={onClose}
      confirmLoading={create.isPending || update.isPending}
      okText={common.actions.save}
      cancelText={common.actions.cancel}
      destroyOnHidden
    >
      <Form form={form} layout="vertical">
        <Form.Item name="name" label={f.name} rules={required}><Input /></Form.Item>
        <Form.Item name="type" label={f.type} rules={required}>
          <Select
            disabled={group !== undefined}
            options={Object.entries(groupTypeLabels).map(([value, label]) => ({ value, label }))}
          />
        </Form.Item>
        <Form.Item name="militaryUnitId" label={f.unit} rules={required}>
          <MilitaryUnitSelect style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="institutionId" label={f.institution}>
          <Select allowClear options={institutions.map((item) => ({ value: item.id, label: item.name }))} />
        </Form.Item>
        {type === 'VOCATIONAL' ? (
          <Form.Item name="professionId" label={f.profession} rules={required} extra={groupLabels.vocationalNote}>
            <Select options={professions.map((item) => ({ value: item.id, label: item.name }))} />
          </Form.Item>
        ) : (
          <Form.Item name="subjectIds" label={f.subjects} rules={required}>
            <Select mode="multiple" options={subjects.map((item) => ({ value: item.id, label: item.name }))} />
          </Form.Item>
        )}
        <Form.Item name="period" label={`${f.startDate} — ${f.endDate}`} rules={required}>
          <DatePicker.RangePicker format={DISPLAY_DATE_FORMAT} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="classroom" label={f.classroom}><Input /></Form.Item>
      </Form>
    </Modal>
  );
}
