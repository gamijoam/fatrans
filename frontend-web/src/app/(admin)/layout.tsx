import { ProtectedRoute } from '@/components/shared/protected-route';
import { LogoutButton } from '@/components/shared/logout-button';
import { Shield, Users, FileText, BarChart3, Settings, CreditCard, ClipboardList } from 'lucide-react';
import { Separator } from '@/components/ui/separator';

const adminMenuItems = [
  { icon: BarChart3, label: 'Dashboard', href: '/admin' },
  { icon: Users, label: 'Socios', href: '/admin/socios' },
  { icon: ClipboardList, label: 'Solicitudes', href: '/admin/solicitudes' },
  { icon: CreditCard, label: 'Créditos', href: '/admin/creditos' },
  { icon: FileText, label: 'Reportes', href: '/admin/reportes' },
  { icon: Settings, label: 'Configuración', href: '/admin/configuracion' },
];

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <ProtectedRoute allowedRoles={['ADMIN', 'ADMINISTRADOR', 'GESTOR', 'SUPER_ADMIN']}>
      <div className="flex min-h-screen">
        <aside className="w-64 bg-gray-900 text-white p-4 flex flex-col">
          <div className="mb-6">
            <div className="flex items-center gap-2">
              <Shield className="h-6 w-6 text-green-400" />
              <h2 className="text-lg font-bold text-white">Admin</h2>
            </div>
            <p className="text-xs text-gray-400">Panel de Administración</p>
          </div>

          <nav className="flex-1 space-y-1">
            {adminMenuItems.map((item) => (
              <a
                key={item.href}
                href={item.href}
                className="flex items-center gap-3 px-3 py-2 text-sm text-gray-300 hover:bg-gray-800 hover:text-white rounded-md transition-colors"
              >
                <item.icon className="h-4 w-4" />
                <span>{item.label}</span>
              </a>
            ))}
          </nav>

          <Separator className="my-4 bg-gray-700" />

          <LogoutButton />
        </aside>

        <main className="flex-1 bg-gray-100">{children}</main>
      </div>
    </ProtectedRoute>
  );
}