import {
  ApartmentOutlined, BankOutlined, EnvironmentOutlined, BellOutlined, ClockCircleOutlined, FileDoneOutlined, FileTextOutlined, ProfileOutlined,
  ApiOutlined, AuditOutlined, BookOutlined, DashboardOutlined, SettingOutlined, TeamOutlined,
  UserOutlined, ReadOutlined, SafetyCertificateOutlined, SolutionOutlined,
} from '@ant-design/icons';
import type { ReactNode } from 'react';
import type { Capability } from '@/features/auth';

/**
 * Menyu bandining ko'rinish sharti: dinamik ruxsat (capability), hamma uchun (`any`) yoki faqat
 * SuperAdmin/Mega SuperAdmin (`permissionManager` — backenddagi statik tekshiruv bilan mos).
 */
export type NavAccess = Capability | 'any' | 'permissionManager';

export type NavItem = { path: string; label: string; icon: ReactNode; access: NavAccess };

export const navItems: NavItem[] = [
  { path: '/dashboard', label: 'Dashboard', icon: <DashboardOutlined />, access: 'dashboardVocational' },
  { path: '/soldiers', label: 'Askarlar', icon: <TeamOutlined />, access: 'soldierRead' },
  { path: '/groups', label: 'Guruhlar', icon: <ReadOutlined />, access: 'groupRead' },
  { path: '/teachers', label: "O'qituvchilar", icon: <SolutionOutlined />, access: 'groupRead' },
  { path: '/subdivisions', label: "Bo'linmalar", icon: <ApartmentOutlined />, access: 'soldierRead' },
  { path: '/assignments', label: 'Biriktirishlar', icon: <BankOutlined />, access: 'assignmentRead' },
  { path: '/admissions', label: 'OTMga qabul', icon: <FileDoneOutlined />, access: 'admissionRead' },
  { path: '/employment', label: "Bandlik ro'yxatlari", icon: <ProfileOutlined />, access: 'employment' },
  { path: '/reports', label: 'Hisobotlar', icon: <FileTextOutlined />, access: 'reports' },
  { path: '/deadlines', label: 'Muddatlar', icon: <ClockCircleOutlined />, access: 'any' },
  { path: '/notifications', label: 'Bildirishnomalar', icon: <BellOutlined />, access: 'any' },
  { path: '/dictionaries', label: "Ma'lumotnomalar", icon: <BookOutlined />, access: 'unitDirections' },
  { path: '/users', label: 'Foydalanuvchilar', icon: <UserOutlined />, access: 'admin' },
  { path: '/admin/role-permissions', label: 'Rol huquqlari', icon: <SafetyCertificateOutlined />, access: 'permissionManager' },
  { path: '/admin/locations', label: 'Hududlar', icon: <EnvironmentOutlined />, access: 'permissionManager' },
  { path: '/integration-logs', label: 'Integratsiya jurnali', icon: <ApiOutlined />, access: 'systemConfig' },
  { path: '/audit', label: 'Audit jurnali', icon: <AuditOutlined />, access: 'systemConfig' },
  { path: '/settings', label: 'Sozlamalar', icon: <SettingOutlined />, access: 'systemConfig' },
];
