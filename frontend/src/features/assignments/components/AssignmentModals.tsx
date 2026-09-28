import { DatePicker, Form, Input, Modal, Radio, Select } from 'antd';
import type { Dayjs } from 'dayjs';
import { useEffect } from 'react';
import { useAuth } from '@/features/auth';
import { MilitaryUnitSelect } from '@/features/organization';
import { useInstitutions } from '@/features/groups';
import { getErrorMessage } from '@/lib/apiClient';
import { API_DATE_FORMAT, DISPLAY_DATE_FORMAT, dayjs } from '@/lib/dayjs';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { useContract, useDecide, usePropose } from '../hooks/useAssignments';
import { assignmentLabels, directionLabels } from '../labels';
import type { Assignment, Direction } from '../types';

const f = assignmentLabels.fields;
const required = [{ required: true, message: common.fields.required }];
const modalProps = { okText: common.actions.save, cancelText: common.actions.cancel, destroyOnHidden: true } as const;

async function save(action: () => Promise<unknown>, onClose: () => void) {
  try {
    await action();
    notify.success(assignmentLabels.done);
    onClose();
  } catch (error) {
    notify.error(getErrorMessage(error));
  }
}

export function ProposeModal({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { user } = useAuth();
  const [form] = Form.useForm<{ militaryUnitId: number; institutionId: number; direction: Direction; note?: string }>();
  const { data: institutions = [] } = useInstitutions();
  const { mutateAsync, isPending } = usePropose();
  useEffect(() => {
    if (open) form.setFieldsValue({ militaryUnitId: user?.militaryUnitId ?? undefined, direction: 'VOCATIONAL' });
  }, [open, form, user]);
  return (
    <Modal open={open} title={assignmentLabels.propose} onCancel={onClose} confirmLoading={isPending} {...modalProps}
      onOk={async () => { const v = await form.validateFields().catch(() => null); if (v) await save(() => mutateAsync(v), onClose); }}>
      <Form form={form} layout="vertical">
        <Form.Item name="militaryUnitId" label={f.unit} rules={required}><MilitaryUnitSelect style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="institutionId" label={f.institution} rules={required}>
          <Select showSearch optionFilterProp="label" options={institutions.map((i) => ({ value: i.id, label: i.name }))} />
        </Form.Item>
        <Form.Item name="direction" label={f.direction} rules={required}>
          <Select options={Object.entries(directionLabels).map(([value, label]) => ({ value, label }))} />
        </Form.Item>
        <Form.Item name="note" label={f.note}><Input.TextArea rows={2} /></Form.Item>
      </Form>
    </Modal>
  );
}

export function DecisionModal({ assignment, onClose }: { assignment: Assignment | null; onClose: () => void }) {
  const [form] = Form.useForm<{ approve: boolean; basisDocument?: string; period?: [Dayjs, Dayjs]; note?: string }>();
  const approve = Form.useWatch('approve', form);
  const { mutateAsync, isPending } = useDecide();
  return (
    <Modal open={assignment !== null} title={assignmentLabels.decide} onCancel={onClose} confirmLoading={isPending} {...modalProps}
      onOk={async () => {
        const v = await form.validateFields().catch(() => null);
        if (!v || !assignment) return;
        await save(() => mutateAsync({ id: assignment.id, request: {
          approve: v.approve, basisDocument: v.basisDocument, note: v.note,
          validFrom: v.period?.[0].format(API_DATE_FORMAT), validTo: v.period?.[1].format(API_DATE_FORMAT),
        } }), onClose);
      }}>
      <Form form={form} layout="vertical" initialValues={{ approve: true }}>
        <Form.Item name="approve" label={f.approve}>
          <Radio.Group options={[{ value: true, label: 'Tasdiqlash' }, { value: false, label: 'Rad etish' }]} />
        </Form.Item>
        {approve && (
          <>
            <Form.Item name="basisDocument" label={f.basis} rules={required}><Input /></Form.Item>
            <Form.Item name="period" label={f.validity}><DatePicker.RangePicker format={DISPLAY_DATE_FORMAT} style={{ width: '100%' }} /></Form.Item>
          </>
        )}
        <Form.Item name="note" label={f.note}><Input.TextArea rows={2} /></Form.Item>
      </Form>
    </Modal>
  );
}

export function ContractModal({ assignment, onClose }: { assignment: Assignment | null; onClose: () => void }) {
  const [form] = Form.useForm<{ contractNo?: string; contractDate?: Dayjs | null; jointPlan?: string }>();
  const { mutateAsync, isPending } = useContract();
  useEffect(() => {
    if (assignment) {
      form.setFieldsValue({
        contractNo: assignment.contractNo ?? undefined,
        contractDate: assignment.contractDate ? dayjs(assignment.contractDate) : null,
        jointPlan: assignment.jointPlan ?? undefined,
      });
    }
  }, [assignment, form]);
  return (
    <Modal open={assignment !== null} title={assignmentLabels.contract} onCancel={onClose} confirmLoading={isPending} {...modalProps}
      onOk={async () => {
        const v = await form.validateFields().catch(() => null);
        if (!v || !assignment) return;
        await save(() => mutateAsync({ id: assignment.id, request: {
          contractNo: v.contractNo, jointPlan: v.jointPlan, contractDate: v.contractDate?.format(API_DATE_FORMAT),
        } }), onClose);
      }}>
      <Form form={form} layout="vertical">
        <Form.Item name="contractNo" label={f.contractNo}><Input /></Form.Item>
        <Form.Item name="contractDate" label={f.contractDate}><DatePicker format={DISPLAY_DATE_FORMAT} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="jointPlan" label={f.jointPlan}><Input.TextArea rows={3} /></Form.Item>
      </Form>
    </Modal>
  );
}
