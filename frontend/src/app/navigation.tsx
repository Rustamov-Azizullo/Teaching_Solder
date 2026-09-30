import {
  ApartmentOutlined, EnvironmentOutlined, BellOutlined, ClockCircleOutlined, FileDoneOutlined, FileTextOutlined, ProfileOutlined,
  AuditOutlined, BookOutlined, DashboardOutlined, SettingOutlined, TeamOutlined,
  UserOutlined, ReadOutlined, SafetyCertificateOutlined, SolutionOutlined,
} from '@ant-design/icons';
import type { ReactNode } from 'react';
import type { Capability } from '@/features/auth';

/**
 * Menyu bandining ko'rinish sharti: dinamik ruxsat (capability), hamma uchun (`any`) yoki faqat
 * SuperAdmin/Mega SuperAdmin (`permissionManager` — backenddagi statik tekshiruv bilan mos).
 */
export type NavAccess = Capability | readonly Capability[] | 'any' | 'permissionManager';

export type NavItem = { path: string; label: string; icon: ReactNode; access: NavAccess };

export const navItems: NavItem[] = [
  { path: '/dashboard', label: 'Dashboard', icon: <DashboardOutlined />, access: 'dashboardVocational' },
  { path: '/soldiers', label: 'Askarlar', icon: <TeamOutlined />, access: 'soldierRead' },
  { path: '/groups', label: 'Guruhlar', icon: <ReadOutlined />, access: 'groupRead' },
  { path: '/teachers', label: "O'qituvchi & Muassasa", icon: <SolutionOutlined />, access: 'groupRead' },
  { path: '/subdivisions', label: "Bo'linmalar", icon: <ApartmentOutlined />, access: 'soldierRead' },
  { path: '/admissions', label: 'OTMga qabul', icon: <FileDoneOutlined />, access: 'admissionRead' },
  { path: '/employment', label: "Bandlik ro'yxatlari", icon: <ProfileOutlined />, access: 'employment' },
  { path: '/reports', label: 'Hisobotlar', icon: <FileTextOutlined />, access: 'reports' },
  { path: '/deadlines', label: 'Muddatlar', icon: <ClockCircleOutlined />, access: 'any' },
  { path: '/notifications', label: 'Bildirishnomalar', icon: <BellOutlined />, access: 'any' },
  { path: '/dictionaries', label: "Ma'lumotnomalar", icon: <BookOutlined />, access: ['dictionaryWrite', 'unitDirections'] },
  { path: '/users', label: 'Foydalanuvchilar', icon: <UserOutlined />, access: 'admin' },
  { path: '/admin/role-permissions', label: 'Rol huquqlari', icon: <SafetyCertificateOutlined />, access: 'permissionManager' },
  { path: '/admin/locations', label: 'Hududlar', icon: <EnvironmentOutlined />, access: 'permissionManager' },
  { path: '/audit', label: 'Audit jurnali', icon: <AuditOutlined />, access: 'systemConfig' },
  { path: '/settings', label: 'Sozlamalar', icon: <SettingOutlined />, access: 'systemConfig' },
];
