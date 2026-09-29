import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Form, Input, Modal, Popconfirm, Select, Space, Tabs, Tag } from 'antd';
import { useEffect, useState } from 'react';
import { NumberedTable, QueryBoundary } from '@/components/ui';
import { useCan } from '@/features/auth';
import { useDictionary } from '@/features/dictionaries';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { InstitutionContracts } from './InstitutionContracts';
import { useDeleteInstitution, useDeleteTeacher, useSaveInstitution, useInstitutions, useSaveTeacher, useTeachers } from '../hooks/useGroups';
import { groupLabels, institutionTypeLabels } from '../labels';
import type { Institution, InstitutionType, Teacher } from '../types';
import { notify } from '@/lib/notify';

type TeacherFormValues = {
  fullName: string; specialtyIds: number[]; institutionId: number;
};

const toOptions = (items: { id: number; name: string }[] = []) => items.map((item) => ({ value: item.id, label: item.name }));
const required = [{ required: true, message: common.fields.required }];
const t = groupLabels.teachers;

type InstitutionModalProps = { institution: Institution | null; open: boolean; onClose: () => void };

function InstitutionModal({ institution, open, onClose }: InstitutionModalProps) {
  const [form] = Form.useForm<{ type: InstitutionType; name: string; professionIds: number[]; subjectIds: number[] }>();
  const professions = useDictionary('PROFESSION', { activeOnly: true });
  const subjects = useDictionary('SUBJECT', { activeOnly: true });
  const { mutateAsync, isPending } = useSaveInstitution(institution?.id);

  useEffect(() => {
    if (!open) return;
    form.resetFields();
    form.setFieldsValue(institution
      ? { type: institution.type, name: institution.name, professionIds: institution.professionIds, subjectIds: institution.subjectIds }
      : { professionIds: [], subjectIds: [] });
  }, [open, institution, form]);

  const handleOk = async () => {
    const values = await form.validateFields();
    try {
      await mutateAsync(values);
      notify.success(common.states.saved);
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };
  return (
    <Modal open={open} title={institution ? t.editInstitution : t.addInstitution} onOk={handleOk} onCancel={onClose}
      confirmLoading={isPending} okText={common.actions.save} cancelText={common.actions.cancel} destroyOnHidden>
      <Form form={form} layout="vertical">
        <Form.Item name="type" label={t.institutionType} rules={required}>
          <Select placeholder={t.institutionType} options={Object.entries(institutionTypeLabels).map(([value, label]) => ({ value, label }))} />
        </Form.Item>
        <Form.Item name="name" label={t.institutionName} rules={required}><Input /></Form.Item>
        <Form.Item name="professionIds" label={t.institutionProfessions}>
          <Select mode="multiple" showSearch optionFilterProp="label" loading={professions.isLoading} options={toOptions(professions.data)} />
        </Form.Item>
        <Form.Item name="subjectIds" label={t.institutionSubjects}>
          <Select mode="multiple" showSearch optionFilterProp="label" loading={subjects.isLoading} options={toOptions(subjects.data)} />
        </Form.Item>
      </Form>
    </Modal>
  );
}

function NameTags({ ids, items = [] }: { ids: number[]; items?: { id: number; name: string }[] }) {
  const names = ids.map((id) => items.find((item) => item.id === id)?.name).filter((name): name is string => Boolean(name));
  return names.length === 0 ? <>—</> : <>{names.map((name) => <Tag key={name}>{name}</Tag>)}</>;
}

function InstitutionsTable({ canEdit, onEdit }: { canEdit: boolean; onEdit: (institution: Institution) => void }) {
  const { data, isLoading, error, refetch } = useInstitutions();
  const professions = useDictionary('PROFESSION');
  const subjects = useDictionary('SUBJECT');
  const { mutateAsync: deleteInstitution, isPending: isDeleting } = useDeleteInstitution();

  const handleDelete = async (id: number) => {
    try {
      await deleteInstitution(id);
      notify.success(t.institutionDeleted);
    } catch (err) {
      notify.error(getErrorMessage(err));
    }
  };

  return (
    <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
      {(institutions) => (
        <NumberedTable<Institution>
          rowKey="id"
          size="middle"
          dataSource={institutions}
          pagination={false}
          scroll={{ x: 'max-content' }}
          columns={[
            { title: t.institutionName, dataIndex: 'name' },
            { title: t.institutionType, dataIndex: 'type', render: (type: InstitutionType) => institutionTypeLabels[type] },
            { title: t.institutionProfessions, key: 'professions', render: (_: unknown, row: Institution) => <NameTags ids={row.professionIds} items={professions.data} /> },
            { title: t.institutionSubjects, key: 'subjects', render: (_: unknown, row: Institution) => <NameTags ids={row.subjectIds} items={subjects.data} /> },
            ...(canEdit
              ? [{
                  title: '',
                  width: 100,
                  render: (_: unknown, row: Institution) => (
                    <Space size={0}>
                      <Button type="text" icon={<EditOutlined />} onClick={() => onEdit(row)} aria-label={common.actions.edit} />
                      <Popconfirm
                        title={t.confirmDeleteInstitution}
                        okText={common.actions.delete}
                        cancelText={common.actions.cancel}
                        okButtonProps={{ danger: true, loading: isDeleting }}
                        onConfirm={() => handleDelete(row.id)}
                      >
                        <Button type="text" danger icon={<DeleteOutlined />} aria-label={common.actions.delete} />
                      </Popconfirm>
                    </Space>
                  ),
                }]
              : []),
          ]}
        />
      )}
    </QueryBoundary>
  );
}

function TeacherModal({ teacher, open, onClose }: { teacher: Teacher | null; open: boolean; onClose: () => void }) {
  const [form] = Form.useForm<TeacherFormValues>();
  const { data: institutions = [], isLoading: isInstitutionsLoading } = useInstitutions();
  const professions = useDictionary('PROFESSION', { activeOnly: true });
  const subjects = useDictionary('SUBJECT', { activeOnly: true });
  const { mutateAsync, isPending } = useSaveTeacher(teacher?.id);
  const institutionId = Form.useWatch('institutionId', form);
  const offered = institutions.find((item) => item.id === institutionId);
  // Muassasada kasb/fan belgilangan bo'lsa, faqat shular tanlanadi; belgilanmagan bo'lsa — hammasi.
  const isOffered = (id: number, ids: number[] | undefined) => !ids || ids.length === 0 || ids.includes(id);
  const specialtyOptions = [
    { label: t.specialtyGroups.professions, options: toOptions(professions.data?.filter((item) => isOffered(item.id, offered?.professionIds))) },
    { label: t.specialtyGroups.subjects, options: toOptions(subjects.data?.filter((item) => isOffered(item.id, offered?.subjectIds))) },
  ];

  useEffect(() => {
    if (!open) return;
    form.resetFields();
    if (teacher) form.setFieldsValue({ fullName: teacher.fullName, institutionId: teacher.institutionId, specialtyIds: teacher.specialtyIds });
  }, [open, teacher, form]);

  const handleOk = async () => {
    const values = await form.validateFields().catch(() => null);
    if (!values) return;
    try {
      await mutateAsync(values);
      notify.success(common.states.saved);
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Modal open={open} title={teacher ? t.editTeacher : t.add} onOk={handleOk} onCancel={onClose} confirmLoading={isPending}
      okText={common.actions.save} cancelText={common.actions.cancel} destroyOnHidden>
      <Form form={form} layout="vertical" onValuesChange={(changed) => { if ('institutionId' in changed) form.setFieldsValue({ specialtyIds: [] }); }}>
        <Form.Item name="fullName" label={common.fields.fullName} rules={required}><Input /></Form.Item>
        <Form.Item name="institutionId" label={t.institution} rules={required}>
          <Select showSearch optionFilterProp="label" loading={isInstitutionsLoading}
            options={institutions.map((item) => ({ value: item.id, label: item.name }))} />
        </Form.Item>
        <Form.Item name="specialtyIds" label={t.specialty} rules={required}>
          <Select mode="multiple" showSearch optionFilterProp="label" placeholder={t.specialtyPlaceholder} loading={professions.isLoading || subjects.isLoading}
            options={specialtyOptions} />
        </Form.Item>
      </Form>
    </Modal>
  );
}

export function TeacherManager() {
  const canEdit = useCan('groupWrite');
  const { data, isLoading, error, refetch } = useTeachers();
  const { mutateAsync: deleteTeacher, isPending: isDeleting } = useDeleteTeacher();
  const [editing, setEditing] = useState<Teacher | null>(null);
  const [isTeacherOpen, setTeacherOpen] = useState(false);
  const [editingInstitution, setEditingInstitution] = useState<Institution | null>(null);
  const [isInstitutionOpen, setInstitutionOpen] = useState(false);

  const openInstitution = (institution: Institution | null) => {
    setEditingInstitution(institution);
    setInstitutionOpen(true);
  };

  const openTeacher = (teacher: Teacher | null) => {
    setEditing(teacher);
    setTeacherOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteTeacher(id);
      notify.success(t.deleted);
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  const teachersTab = (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      {canEdit && <Button type="primary" icon={<PlusOutlined />} onClick={() => openTeacher(null)}>{t.add}</Button>}
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(teachers) => (
          <NumberedTable<Teacher>
            rowKey="id"
            dataSource={teachers}
            pagination={false}
            scroll={{ x: 'max-content' }}
            columns={[
              { title: common.fields.fullName, dataIndex: 'fullName' },
              { title: t.specialty, dataIndex: 'specialty' },
              { title: t.institution, dataIndex: 'institutionName' },
              ...(canEdit
                ? [{
                    title: '',
                    render: (_: unknown, row: Teacher) => (
                      <Space size={0}>
                        <Button type="text" icon={<EditOutlined />} onClick={() => openTeacher(row)} aria-label={common.actions.edit} />
                        <Popconfirm
                          title={t.confirmDelete}
                          okText={common.actions.delete}
                          cancelText={common.actions.cancel}
                          okButtonProps={{ danger: true, loading: isDeleting }}
                          onConfirm={() => handleDelete(row.id)}
                        >
                          <Button type="text" danger icon={<DeleteOutlined />} aria-label={common.actions.delete} />
                        </Popconfirm>
                      </Space>
                    ),
                  }]
                : []),
            ]}
          />
        )}
      </QueryBoundary>
    </Space>
  );

  return (
    <>
      <Tabs
        items={[
          { key: 'teachers', label: t.tabTeachers, children: teachersTab },
          {
            key: 'institutions',
            label: t.institutionsTitle,
            children: (
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                {canEdit && <Button type="primary" icon={<PlusOutlined />} onClick={() => openInstitution(null)}>{t.addInstitution}</Button>}
                <InstitutionsTable canEdit={canEdit} onEdit={openInstitution} />
              </Space>
            ),
          },
          { key: 'contracts', label: t.tabContracts, children: <InstitutionContracts canEdit={canEdit} /> },
        ]}
      />
      <TeacherModal teacher={editing} open={isTeacherOpen} onClose={() => setTeacherOpen(false)} />
      <InstitutionModal institution={editingInstitution} open={isInstitutionOpen} onClose={() => setInstitutionOpen(false)} />
    </>
  );
}
