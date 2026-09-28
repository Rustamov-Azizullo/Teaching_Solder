import type { MessageInstance } from 'antd/es/message/interface';

let messageApi: MessageInstance | null = null;

/** Ant Design <App> konteksti bilan ishlaydigan xabarlar (tema va lokalni hisobga oladi). */
export function bindNotify(instance: MessageInstance): void {
  messageApi = instance;
}

export const notify = {
  success: (content: string) => messageApi?.success(content),
  error: (content: string) => messageApi?.error(content),
  warning: (content: string) => messageApi?.warning(content),
};
