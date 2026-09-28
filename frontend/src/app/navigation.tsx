import {
  ApartmentOutlined, BankOutlined, BellOutlined, ClockCircleOutlined, FileDoneOutlined, FileTextOutlined, ProfileOutlined,
  SafetyCertificateOutlined, ToolOutlined, ApiOutlined, AuditOutlined, BookOutlined, CheckSquareOutlined, DashboardOutlined, SettingOutlined, TeamOutlined,
  UserOutlined, ReadOutlined, SolutionOutlined,
} from '@ant-design/icons';
import type { ReactNode } from 'react';
import type { Capability } from '@/features/auth';

export type NavItem = { path: string; label: string; icon: ReactNode; capability: Capability | 'any' };

export const navItems: NavItem[] = [
  { path: '/dashboard', label: 'Dashboard', icon: <DashboardOutlined />, capability: 'dashboardVocational' },
  { path: '/attendance', label: 'Davomat', icon: <CheckSquareOutlined />, capability: 'attendanceWrite' },
  { path: '/soldiers', label: 'Askarlar', icon: <TeamOutlined />, capability: 'soldierRead' },
  { path: '/groups', label: 'Guruhlar', icon: <ReadOutlined />, capability: 'groupRead' },
  { path: '/teachers', label: "O'qituvchilar", icon: <SolutionOutlined />, capability: 'groupRead' },
  { path: '/subdivisions', label: "Bo'linmalar", icon: <ApartmentOutlined />, capability: 'soldierRead' },
  { path: '/assignments', label: 'Biriktirishlar', icon: <BankOutlined />, capability: 'assignmentRead' },
  { path: '/facilities', label: "O'quv-moddiy baza", icon: <ToolOutlined />, capability: 'groupRead' },
  { path: '/admissions', label: 'OTMga qabul', icon: <FileDoneOutlined />, capability: 'admissionRead' },
  { path: '/employment', label: "Bandlik ro'yxatlari", icon: <ProfileOutlined />, capability: 'employment' },
  { path: '/kta', label: 'KTA solishtirish', icon: <SafetyCertificateOutlined />, capability: 'ktaCompare' },
  { path: '/reports', label: 'Hisobotlar', icon: <FileTextOutlined />, capability: 'reports' },
  { path: '/deadlines', label: 'Muddatlar', icon: <ClockCircleOutlined />, capability: 'any' },
  { path: '/notifications', label: 'Bildirishnomalar', icon: <BellOutlined />, capability: 'any' },
  { path: '/dictionaries', label: "Ma'lumotnomalar", icon: <BookOutlined />, capability: 'unitDirections' },
  { path: '/users', label: 'Foydalanuvchilar', icon: <UserOutlined />, capability: 'admin' },
  { path: '/integration-logs', label: 'Integratsiya jurnali', icon: <ApiOutlined />, capability: 'admin' },
  { path: '/audit', label: 'Audit jurnali', icon: <AuditOutlined />, capability: 'admin' },
  { path: '/settings', label: 'Sozlamalar', icon: <SettingOutlined />, capability: 'admin' },
];
