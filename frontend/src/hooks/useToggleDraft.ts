import { useCallback, useMemo, useState } from 'react';

export type ToggleDraft<K extends string> = {
  /** Faqat server qiymatidan farq qiladigan (saqlanmagan) qiymatlar. */
  changes: ReadonlyMap<K, boolean>;
  isDirty: boolean;
  valueOf: (key: K, savedValue: boolean) => boolean;
  set: (key: K, value: boolean, savedValue: boolean) => void;
  reset: () => void;
};

/**
 * Checkbox ro'yxati/matritsasi uchun saqlanmagan o'zgarishlar. Qiymat server holatiga qaytarilsa,
 * o'zgarish ro'yxatdan chiqariladi — shuning uchun serverga faqat haqiqiy farqlar yuboriladi.
 */
export function useToggleDraft<K extends string>(): ToggleDraft<K> {
  const [changes, setChanges] = useState<ReadonlyMap<K, boolean>>(() => new Map());

  const valueOf = useCallback((key: K, savedValue: boolean) => changes.get(key) ?? savedValue, [changes]);

  const set = useCallback((key: K, value: boolean, savedValue: boolean) => {
    setChanges((previous) => {
      const next = new Map(previous);
      if (value === savedValue) next.delete(key);
      else next.set(key, value);
      return next;
    });
  }, []);

  const reset = useCallback(() => setChanges(new Map()), []);

  return useMemo(
    () => ({ changes, isDirty: changes.size > 0, valueOf, set, reset }),
    [changes, valueOf, set, reset],
  );
}
