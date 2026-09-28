export type Role = 'MEGA_SUPER_ADMIN' | 'SUPER_ADMIN' | 'ADMIN' | 'USER';

/** Backenddagi `LocationLevel` enum: respublika (ildiz) -> harbiy okrug -> harbiy qism. */
export type LocationLevel = 'REPUBLIC' | 'DISTRICT' | 'UNIT';

/** Backenddagi `Permission` enum kalitlari (rol-ruxsat matritsasi va shaxsiy ruxsatlar shu kalitlar bilan ishlaydi). */
export type PermissionKey =
  | 'ADMIN'
  | 'SYSTEM_CONFIG'
  | 'DICTIONARY_WRITE'
  | 'UNIT_DIRECTIONS'
  | 'SOLDIER_READ'
  | 'SOLDIER_WRITE'
  | 'QUESTIONNAIRE_READ'
  | 'QUESTIONNAIRE_WRITE'
  | 'ATTACHMENT_WRITE'
  | 'ASSIGNMENT_READ'
  | 'ASSIGNMENT_PROPOSE'
  | 'ASSIGNMENT_DECIDE'
  | 'DEADLINE_MANAGE'
  | 'RESULT_READ'
  | 'RESULT_WRITE'
  | 'ADMISSION_READ'
  | 'ADMISSION_WRITE'
  | 'EMPLOYMENT'
  | 'TRANSFER'
  | 'GROUP_READ'
  | 'GROUP_WRITE'
  | 'GROUP_LEADER_ASSIGN'
  | 'SCHEDULE_WRITE'
  | 'SCHEDULE_TIME_OVERRIDE'
  | 'REPORTS'
  | 'DASHBOARD_VOCATIONAL'
  | 'DASHBOARD_OTM'
  | 'DASHBOARD_SURVEYS';

export type AuthUser = {
  id: number;
  username: string;
  fullName: string;
  role: Role;
  roleLabel: string;
  locationId: number | null;
  locationName: string | null;
  locationLevel: LocationLevel | null;
  /** Hududdan hisoblanadi (faqat qism darajasida); guruh/biriktirish formalarida standart qism sifatida o'qiladi. */
  militaryUnitId: number | null;
  active: boolean;
  /** Foydalanuvchining amaldagi barcha ruxsatlari (rol + shaxsiy). */
  permissions: PermissionKey[];
};

export type LoginResponse = {
  accessToken: string;
  expiresInSeconds: number;
  user: AuthUser;
};

export type AuthStatus = 'loading' | 'authenticated' | 'anonymous';
