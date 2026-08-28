import { createBrowserRouter } from 'react-router-dom';
import { AppLayout } from '@/app/AppLayout';
import { PerformanceListPage } from '@/pages/PerformanceListPage';
import { PerformanceDetailPage } from '@/pages/PerformanceDetailPage';
import { NotFoundPage } from '@/pages/NotFoundPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <PerformanceListPage /> },
      { path: 'performances/:id', element: <PerformanceDetailPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);
