import { Alert, Button, Modal, Table } from 'antd';
import { useEffect, useState } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { common } from '@/lib/i18n';
import { notify } from '@/lib/notify';
import { useSourceRefresh } from '../hooks/useSoldiers';
import { soldierLabels } from '../labels';
import type { FieldDiff } from '../types';

const t = soldierLabels.refresh;

/** Manba tizimdagi ma'lumot farq qilsa, farqlar ko'rsatiladi; xodim tasdiqlagan maydonlar yangilanadi. */
export function SourceRefreshModal({ soldierId, open, onClose }: { soldierId: number; open: boolean; onClose: () => void }) {
  const { diff, apply } = useSourceRefresh(soldierId);
  const [selected, setSelected] = useState<string[]>([]);
  const { mutate: loadDiff } = diff;

  useEffect(() => {
    if (open) {
      setSelected([]);
      loadDiff();
    }
  }, [open, loadDiff]);

  const handleApply = async () => {
    try {
      await apply.mutateAsync(selected);
      notify.success(t.applied);
      onClose();
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  const diffs = diff.data ?? [];
  return (
    <Modal open={open} title={t.title} onCancel={onClose} width={640}
      footer={[
        <Button key="c" onClick={onClose}>{common.actions.close}</Button>,
        <Button key="a" type="primary" disabled={selected.length === 0} loading={apply.isPending} onClick={handleApply}>{t.apply}</Button>,
      ]}>
      {diff.isError && <Alert type="error" showIcon message={getErrorMessage(diff.error)} />}
      {diff.isSuccess && diffs.length === 0 && <Alert type="success" showIcon message={t.none} />}
      {diffs.length > 0 && (
        <>
          <Alert type="info" showIcon message={t.hint} style={{ marginBottom: 12 }} />
          <Table<FieldDiff>
            rowKey="field"
            size="small"
            pagination={false}
            rowSelection={{ selectedRowKeys: selected, onChange: (keys) => setSelected(keys as string[]) }}
            dataSource={diffs}
            columns={[
              { title: common.fields.name, dataIndex: 'label' },
              { title: t.current, dataIndex: 'current' },
              { title: t.incoming, dataIndex: 'incoming' },
            ]}
          />
        </>
      )}
    </Modal>
  );
}
