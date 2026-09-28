import type { ComponentType } from 'react';
import { createBrowserRouter, type RouteObject } from 'react-router-dom';
import { RequireAuth, RequireCapability, type Capability } from '@/features/auth';
import { LoginPage } from '@/pages/LoginPage';
import { HomeRedirect } from './HomeRedirect';
import { AppLayout } from './layout/AppLayout';

type PageModule = Record<string, ComponentType>;

/** Sahifani kerak bo'lganda yuklaydi (past tezlikli internetda boshlang'ich yuklanish kichik bo'ladi). */
function page(path: string, load: () => Promise<PageModule>, exportName: string, capability?: Capability): RouteObject {
  return {
    path,
    lazy: async () => {
      const Page = (await load())[exportName];
      const Component = () =>
        capability ? (
          <RequireCapability capability={capability}>
            <Page />
          </RequireCapability>
        ) : (
          <Page />
        );
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
          page('attendance', () => import('@/pages/AttendancePage'), 'AttendancePage', 'attendanceWrite'),
          page('attendance/:lessonId', () => import('@/pages/AttendanceSheetPage'), 'AttendanceSheetPage', 'groupRead'),
          page('soldiers', () => import('@/pages/SoldiersPage'), 'SoldiersPage', 'soldierRead'),
          page('soldiers/new', () => import('@/pages/SoldierNewPage'), 'SoldierNewPage', 'soldierWrite'),
          page('soldiers/:soldierId', () => import('@/pages/SoldierDetailPage'), 'SoldierDetailPage', 'soldierRead'),
          page('soldiers/:soldierId/edit', () => import('@/pages/SoldierEditPage'), 'SoldierEditPage', 'soldierWrite'),
          page('soldiers/:soldierId/questionnaire', () => import('@/pages/QuestionnairePage'), 'QuestionnairePage', 'questionnaireRead'),
          page('groups', () => import('@/pages/GroupsPage'), 'GroupsPage', 'groupRead'),
          page('groups/:groupId', () => import('@/pages/GroupDetailPage'), 'GroupDetailPage', 'groupRead'),
          page('teachers', () => import('@/pages/TeachersPage'), 'TeachersPage', 'groupRead'),
          page('dictionaries', () => import('@/pages/DictionariesPage'), 'DictionariesPage', 'unitDirections'),
          page('users', () => import('@/pages/UsersPage'), 'UsersPage', 'admin'),
          page('audit', () => import('@/pages/AuditPage'), 'AuditPage', 'admin'),
          page('settings', () => import('@/pages/SettingsPage'), 'SettingsPage', 'admin'),
          page('profile', () => import('@/pages/ProfilePage'), 'ProfilePage'),
          page('subdivisions', () => import('@/pages/SubdivisionsPage'), 'SubdivisionsPage', 'soldierRead'),
          page('assignments', () => import('@/pages/AssignmentsPage'), 'AssignmentsPage', 'assignmentRead'),
          page('facilities', () => import('@/pages/FacilitiesPage'), 'FacilitiesPage', 'groupRead'),
          page('admissions', () => import('@/pages/AdmissionsPage'), 'AdmissionsPage', 'admissionRead'),
          page('employment', () => import('@/pages/EmploymentPage'), 'EmploymentPage', 'employment'),
          page('kta', () => import('@/pages/KtaPage'), 'KtaPage', 'ktaCompare'),
          page('reports', () => import('@/pages/ReportsPage'), 'ReportsPage', 'reports'),
          page('deadlines', () => import('@/pages/DeadlinesPage'), 'DeadlinesPage'),
          page('notifications', () => import('@/pages/NotificationsPage'), 'NotificationsPage'),
          page('integration-logs', () => import('@/pages/IntegrationLogsPage'), 'IntegrationLogsPage', 'admin'),
          page('*', () => import('@/pages/NotFoundPage'), 'NotFoundPage'),
        ],
      },
    ],
  },
]);
