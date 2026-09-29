import { Table, type TableProps } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';

const FIRST_PAGE = 1;
const DEFAULT_PAGE_SIZE = 10;
const NUMBER_COLUMN_WIDTH = 64;

/**
 * Antd `Table` + birinchi ustunda tartib raqami. Raqam sahifalar bo'ylab davom etadi (2-sahifa 16 dan boshlanadi),
 * shu sababli jadval sahifa holatini o'zi kuzatadi; tashqaridan berilgan `current`/`pageSize` ustuvor.
 */
export function NumberedTable<T extends object>({ columns = [], pagination, onChange, dataSource, ...rest }: TableProps<T>) {
  const options = pagination === false ? null : pagination ?? {};
  const [page, setPage] = useState({ current: FIRST_PAGE, pageSize: options?.defaultPageSize ?? DEFAULT_PAGE_SIZE });
  const pageSize = options?.pageSize ?? page.pageSize;
  const total = options?.total ?? dataSource?.length ?? 0;
  const lastPage = Math.max(FIRST_PAGE, Math.ceil(total / pageSize));
  const current = Math.min(options?.current ?? page.current, lastPage);
  const offset = options ? (current - FIRST_PAGE) * pageSize : 0;

  const numberColumn: ColumnsType<T>[number] = {
    title: '№', key: '__rowNumber', width: NUMBER_COLUMN_WIDTH, align: 'right',
    render: (_: unknown, __: T, index: number) => offset + index + 1,
  };

  return (
    <Table<T>
      {...rest}
      dataSource={dataSource}
      columns={[numberColumn, ...columns]}
      pagination={options === null ? false : { ...options, current, pageSize }}
      onChange={(next, filters, sorter, extra) => {
        setPage({ current: next.current ?? FIRST_PAGE, pageSize: next.pageSize ?? pageSize });
        onChange?.(next, filters, sorter, extra);
      }}
    />
  );
}
