import { Form } from 'antd';
import { useCallback, useState } from 'react';
import { getErrorMessage } from '@/lib/apiClient';
import { soldierLabels } from '../labels';
import type { FieldSource, Soldier, SoldierFormValues, SourceLookup } from '../types';
import { useSaveSoldier } from './useSoldiers';
import { SOURCE_TRACKED_FIELDS, toFormValues, toPrefill, toSoldierRequest } from '../utils/soldierMapper';
import { notify } from '@/lib/notify';

/** Askar formasining holati: manba belgilari (integratsiya/qo'lda), oldindan to'ldirish va saqlash. */
export function useSoldierForm(initial: Soldier | undefined, onSaved: (soldier: Soldier) => void) {
  const [form] = Form.useForm<SoldierFormValues>();
  const [integrationFields, setIntegrationFields] = useState<ReadonlySet<string>>(() =>
    new Set(
      initial
        ? SOURCE_TRACKED_FIELDS.filter((field) => initial.fieldSources[field]?.source === 'INTEGRATION')
        : [],
    ),
  );
  const { mutateAsync, isPending } = useSaveSoldier(initial?.id);

  const prefillFrom = useCallback(
    (lookup: SourceLookup) => {
      const { values, fields } = toPrefill(lookup);
      form.setFieldsValue(values);
      setIntegrationFields(new Set(fields));
    },
    [form],
  );

  /** Foydalanuvchi integratsiyadan kelgan maydonni o'zgartirsa, u "qo'lda" deb belgilanadi. */
  const handleValuesChange = useCallback((changed: Partial<SoldierFormValues>) => {
    const editedKeys = Object.keys(changed);
    setIntegrationFields((current) => {
      if (!editedKeys.some((key) => current.has(key))) return current;
      return new Set([...current].filter((field) => !editedKeys.includes(field)));
    });
  }, []);

  const sourceOf = useCallback(
    (field: string): FieldSource | undefined => {
      if (integrationFields.has(field)) return 'INTEGRATION';
      return initial?.fieldSources[field]?.source === 'INTEGRATION' ? 'MANUAL' : initial?.fieldSources[field]?.source;
    },
    [integrationFields, initial],
  );

  const submit = async (values: SoldierFormValues) => {
    try {
      const saved = await mutateAsync(toSoldierRequest(values, integrationFields));
      notify.success(soldierLabels.saved);
      onSaved(saved);
    } catch (error) {
      notify.error(getErrorMessage(error));
    }
  };

  return {
    form,
    initialValues: initial ? toFormValues(initial) : undefined,
    prefillFrom,
    handleValuesChange,
    sourceOf,
    submit,
    isSaving: isPending,
  };
}
