import type { AxiosResponse } from 'axios';

const FALLBACK_FILE_NAME = 'fayl';

/** Server yuborgan faylni (Content-Disposition dagi nom bilan) brauzerda yuklab olishni boshlaydi. */
export function saveBlobResponse(response: AxiosResponse<Blob>, fallbackName = FALLBACK_FILE_NAME): void {
  const disposition = response.headers['content-disposition'] as string | undefined;
  const match = disposition?.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i);
  const fileName = match ? decodeURIComponent(match[1]) : fallbackName;
  const url = URL.createObjectURL(response.data);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(url);
}
