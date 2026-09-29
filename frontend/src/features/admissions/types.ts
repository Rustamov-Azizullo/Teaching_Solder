export type StudyForm = 'FULL_TIME' | 'EVENING' | 'PART_TIME' | 'DISTANCE';
export type OnlineStatus = 'NONE' | 'AGREED' | 'STUDYING';

export type AdmissionRow = {
  soldierId: number;
  fullName: string;
  pinfl: string;
  districtId: number;
  districtName: string;
  unitId: number;
  unitName: string;
  bmbaRegistered: boolean;
  benefitsUploaded: boolean;
  testParticipated: boolean;
  testScore: number | null;
  admitted: boolean;
  university: string | null;
  studyDirection: string | null;
  studyForm: StudyForm | null;
  onlineStatus: OnlineStatus;
  serviceEndDate: string | null;
  bmbaSyncedAt: string | null;
};

export type AdmissionUpdate = Omit<AdmissionRow, 'soldierId' | 'fullName' | 'pinfl' | 'districtId' | 'districtName' | 'unitId' | 'unitName' | 'serviceEndDate' | 'bmbaSyncedAt'>;
export type FunnelStep = { label: string; count: number };
export type SyncResult = { synced: number; notFound: number; failed: number };
