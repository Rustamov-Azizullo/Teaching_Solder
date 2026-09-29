import type { ComponentType, ReactNode } from 'react';
import { createBrowserRouter, type RouteObject } from 'react-router-dom';
import { RequireAuth, RequireCapability, RequirePermissionManager, type Capability } from '@/features/auth';
import { LoginPage } from '@/pages/LoginPage';
import { HomeRedirect } from './HomeRedirect';
import { AppLayout } from './layout/AppLayout';

type PageModule = Record<string, ComponentType>;

/** Sahifaga kirish sharti: dinamik ruxsat (capability) yoki faqat SuperAdmin/Mega SuperAdmin. */
type RouteAccess = Capability | readonly Capability[] | 'permissionManager';

function guard(access: RouteAccess | undefined, content: ReactNode): ReactNode {
  if (!access) return content;
  if (access === 'permissionManager') return <RequirePermissionManager>{content}</RequirePermissionManager>;
  return <RequireCapability capability={access}>{content}</RequireCapability>;
}

/** Sahifani kerak bo'lganda yuklaydi (past tezlikli internetda boshlang'ich yuklanish kichik bo'ladi). */
function page(path: string, load: () => Promise<PageModule>, exportName: string, access?: RouteAccess): RouteObject {
  return {
    path,
    lazy: async () => {
      const Page = (await load())[exportName];
      const Component = () => guard(access, <Page />);
      return { Component };
    },
  };
}

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    element: <RequireAuth />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { index: true, element: <HomeRedirect /> },
          page('dashboard', () => import('@/pages/DashboardPage'), 'DashboardPage'),
          page('soldiers', () => import('@/pages/SoldiersPage'), 'SoldiersPage', 'soldierRead'),
          page('soldiers/new', () => import('@/pages/SoldierNewPage'), 'SoldierNewPage', 'soldierWrite'),
          page('soldiers/:soldierId', () => import('@/pages/SoldierDetailPage'), 'SoldierDetailPage', 'soldierRead'),
          page('soldiers/:soldierId/edit', () => import('@/pages/SoldierEditPage'), 'SoldierEditPage', 'soldierWrite'),
          page('soldiers/:soldierId/questionnaire', () => import('@/pages/QuestionnairePage'), 'QuestionnairePage', 'questionnaireRead'),
          page('groups', () => import('@/pages/GroupsPage'), 'GroupsPage', 'groupRead'),
          page('groups/:groupId', () => import('@/pages/GroupDetailPage'), 'GroupDetailPage', 'groupRead'),
          page('teachers', () => import('@/pages/TeachersPage'), 'TeachersPage', 'groupRead'),
          page('dictionaries', () => import('@/pages/DictionariesPage'), 'DictionariesPage', ['dictionaryWrite', 'unitDirections']),
          page('users', () => import('@/pages/UsersPage'), 'UsersPage', 'admin'),
          page('audit', () => import('@/pages/AuditPage'), 'AuditPage', 'systemConfig'),
          page('settings', () => import('@/pages/SettingsPage'), 'SettingsPage', 'systemConfig'),
          page('admin/role-permissions', () => import('@/pages/RolePermissionsPage'), 'RolePermissionsPage', 'permissionManager'),
          page('admin/locations', () => import('@/pages/LocationsPage'), 'LocationsPage', 'permissionManager'),
          page('profile', () => import('@/pages/ProfilePage'), 'ProfilePage'),
          page('subdivisions', () => import('@/pages/SubdivisionsPage'), 'SubdivisionsPage', 'soldierRead'),
          page('admissions', () => import('@/pages/AdmissionsPage'), 'AdmissionsPage', 'admissionRead'),
          page('employment', () => import('@/pages/EmploymentPage'), 'EmploymentPage', 'employment'),
          page('reports', () => import('@/pages/ReportsPage'), 'ReportsPage', 'reports'),
          page('deadlines', () => import('@/pages/DeadlinesPage'), 'DeadlinesPage'),
          page('notifications', () => import('@/pages/NotificationsPage'), 'NotificationsPage'),
          page('integration-logs', () => import('@/pages/IntegrationLogsPage'), 'IntegrationLogsPage', 'systemConfig'),
          page('*', () => import('@/pages/NotFoundPage'), 'NotFoundPage'),
        ],
      },
    ],
  },
]);
