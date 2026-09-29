export type AttachmentOwnerType = 'SOLDIER' | 'RESULT' | 'QUESTIONNAIRE';

export type Attachment = {
  id: number;
  kind: string | null;
  fileName: string;
  contentType: string;
  sizeBytes: number;
  uploadedBy: string;
  uploadedAt: string;
};
