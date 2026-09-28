export type Role =
  | 'SYSTEM_ADMIN'
  | 'HKTB'
  | 'JTB'
  | 'TMIBB'
  | 'DISTRICT_OFFICER'
  | 'UNIT_COMMANDER'
  | 'UNIT_OPERATOR'
  | 'COMBAT_TRAINING_DEPT'
  | 'EDUCATION_DEPT'
  | 'GROUP_LEADER'
  | 'PSYCHOLOGIST';

export type AuthUser = {
  id: number;
  username: string;
  fullName: string;
  role: Role;
  roleLabel: string;
  militaryDistrictId: number | null;
  militaryUnitId: number | null;
  militaryUnitName: string | null;
  active: boolean;
  twoFactorEnabled: boolean;
};

export type TwoFactorSetup = { secret: string; otpauthUri: string };

export type LoginResponse = {
  accessToken: string;
  expiresInSeconds: number;
  user: AuthUser;
  twoFactorEnabled: boolean;
  twoFactorSetupRequired: boolean;
};

export type AuthStatus = 'loading' | 'authenticated' | 'anonymous';
