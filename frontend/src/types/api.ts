export type NamedRef = { id: number; name: string };

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type ApiErrorBody = {
  status: number;
  message: string;
  fieldErrors?: Record<string, string>;
  code?: string;
};
