export type DictionaryType =
  | 'SUBJECT'
  | 'PROFESSION'
  | 'PROFESSION_DIRECTION'
  | 'FUTURE_PLAN'
  | 'LANGUAGE'
  | 'KINSHIP'
  | 'AWARD_KIND';

export type DictionaryItem = {
  id: number;
  type: DictionaryType;
  code: string;
  name: string;
  description: string | null;
  hours: number | null;
  active: boolean;
};

export type DictionaryTypeInfo = { code: DictionaryType; label: string };

export type DictionaryItemInput = {
  code: string;
  name: string;
  description?: string;
  hours?: number | null;
  active: boolean;
};
