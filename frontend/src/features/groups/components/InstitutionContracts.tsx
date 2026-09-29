import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Form, Modal, Popconfirm, Select, Space } from 'antd';
import { useEffect, useState } from 'react';
import { NumberedTable, QueryBoundary } from '@/components/ui';
import { DistrictUnitSelect, type DistrictUnitSelection } from '@/features/organization';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { useContracts, useCreateContract, useDeleteContract, useInstitutions, useUpdateContract } from '../hooks/useGroups';
import { groupLabels, institutionTypeLabels } from '../labels';
import type { InstitutionContract } from '../types';

const t = groupLabels.teachers;

type ContractModalProps = { contract: InstitutionContract | null; open: boolean; onClose: () => void };

/** Yangi biriktirish yoki (`contract` berilsa) mavjudini o'zgartirish. */
function ContractModal({ contract, open, onClose }: ContractModalProps) {
  const { data: institutions = [] } = useInstitutions();
  const create = useCreateContract();
  const update = useUpdateContract();
  const [place, setPlace] = useState<DistrictUnitSelection>({});
  const [institutionId, setInstitutionId] = useState<number | undefined>();
  const canSave = place.unitId !== undefined && institutionId !== undefined;

  useEffect(() => {
    if (!open) return;
    setPlace({ unitId: contract?.unitId });
    setInstitutionId(contract?.institutionId);
  }, [open, contract]);

  const handleOk = async () => {
    if (place.unitId === undefined || institutionId === undefined) return;
    const next = { institutionId, unitId: place.unitId };
    try {
      if (contract) await update.mutateAsync({ current: { institutionId: contract.institutionId, unitId: contract.unitId }, next });
      else await create.mutateAsync(next);
      notify.success(t.contractSaved);
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Modal open={open} title={contract ? t.editContract : t.contractTitle} onOk={handleOk} onCancel={onClose}
      confirmLoading={create.isPending || update.isPending} okButtonProps={{ disabled: !canSave }}
      okText={common.actions.save} cancelText={common.actions.cancel} destroyOnHidden>
      <Form layout="vertical">
        <Form.Item label={t.contractUnit} required>
          <DistrictUnitSelect key={contract?.unitId ?? 'new'} value={place} onChange={setPlace} />
        </Form.Item>
        <Form.Item label={t.contractInstitution} required>
          <Select showSearch optionFilterProp="label" value={institutionId} onChange={setInstitutionId}
            options={institutions.map((item) => ({ value: item.id, label: item.name }))} />
        </Form.Item>
      </Form>
    </Modal>
  );
}

/** "Muassasani harbiy qismga biriktirish": qaysi qism qaysi muassasa bilan shartnoma tuzgani. */
export function InstitutionContracts({ canEdit }: { canEdit: boolean }) {
  const { data, isLoading, error, refetch } = useContracts();
  const { mutateAsync: remove, isPending: isRemoving } = useDeleteContract();
  const [isOpen, setOpen] = useState(false);
  const [editing, setEditing] = useState<InstitutionContract | null>(null);
  const openModal = (contract: InstitutionContract | null) => { setEditing(contract); setOpen(true); };

  const handleDelete = async (row: InstitutionContract) => {
    try {
      await remove({ institutionId: row.institutionId, unitId: row.unitId });
      notify.success(t.contractDeleted);
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      {canEdit && <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal(null)}>{t.addContract}</Button>}
      <QueryBoundary isLoading={isLoading} error={error} data={data} onRetry={refetch}>
        {(contracts) => (
          <NumberedTable<InstitutionContract>
            rowKey={(row) => `${row.institutionId}-${row.unitId}`}
            size="middle"
            dataSource={contracts}
            scroll={{ x: 'max-content' }}
            columns={[
              { title: t.contractDistrict, dataIndex: 'districtName' },
              { title: t.contractUnit, dataIndex: 'unitName' },
              { title: t.contractInstitution, dataIndex: 'institutionName' },
              { title: t.institutionType, dataIndex: 'institutionType', render: (type: InstitutionContract['institutionType']) => institutionTypeLabels[type] },
              ...(canEdit ? [{
                title: '', width: 100,
                render: (_: unknown, row: InstitutionContract) => (
                  <Space size={0}>
                  <Button type="text" icon={<EditOutlined />} onClick={() => openModal(row)} aria-label={common.actions.edit} />
                  <Popconfirm title={t.confirmDeleteContract} okText={common.actions.delete} cancelText={common.actions.cancel}
                    okButtonProps={{ danger: true, loading: isRemoving }} onConfirm={() => handleDelete(row)}>
                    <Button type="text" danger icon={<DeleteOutlined />} aria-label={common.actions.delete} />
                  </Popconfirm>
                  </Space>
                ),
              }] : []),
            ]}
          />
        )}
      </QueryBoundary>
      <ContractModal contract={editing} open={isOpen} onClose={() => setOpen(false)} />
    </Space>
  );
}
