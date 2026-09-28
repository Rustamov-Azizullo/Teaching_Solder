export type AttachmentOwnerType = 'SOLDIER' | 'ASSIGNMENT' | 'FACILITY' | 'RESULT' | 'QUESTIONNAIRE';

export type Attachment = {
  id: number;
  kind: string | null;
  fileName: string;
  contentType: string;
  sizeBytes: number;
  uploadedBy: string;
  uploadedAt: string;
};
