import { ProtectedRoute } from '@/components/shared/protected-route';

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <ProtectedRoute allowedRoles={['ADMIN', 'ADMINISTRADOR', 'GESTOR', 'SUPER_ADMIN']}>
      <div className="flex min-h-screen">
        <aside className="w-64 bg-gray-900 text-white p-4">Admin Sidebar</aside>
        <main className="flex-1">{children}</main>
      </div>
    </ProtectedRoute>
  );
}