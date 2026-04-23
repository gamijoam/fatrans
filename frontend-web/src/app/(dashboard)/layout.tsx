import { ProtectedRoute } from '@/components/shared/protected-route';
import { LogoutButton } from '@/components/shared/logout-button';
import { LayoutDashboard, Wallet, ArrowLeftRight, CreditCard, FileText, User } from 'lucide-react';
import { Separator } from '@/components/ui/separator';

const menuItems = [
  { icon: LayoutDashboard, label: 'Dashboard', href: '/dashboard' },
  { icon: Wallet, label: 'Cuentas', href: '/dashboard/cuentas' },
  { icon: ArrowLeftRight, label: 'Movimientos', href: '/dashboard/movimientos' },
  { icon: CreditCard, label: 'Créditos', href: '/dashboard/creditos' },
  { icon: FileText, label: 'Documentos', href: '/dashboard/documentos' },
  { icon: User, label: 'Mi Perfil', href: '/dashboard/perfil' },
];

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <ProtectedRoute allowedRoles={['SOCIO']}>
      <div className="flex min-h-screen">
        <aside className="w-64 bg-gray-50 border-r border-gray-200 p-4 flex flex-col">
          <div className="mb-6">
            <h2 className="text-lg font-bold text-green-600">FATRANS</h2>
            <p className="text-xs text-gray-500">Fondo de Ahorro</p>
          </div>

          <nav className="flex-1 space-y-1">
            {menuItems.map((item) => (
              <a
                key={item.href}
                href={item.href}
                className="flex items-center gap-3 px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-md transition-colors"
              >
                <item.icon className="h-4 w-4" />
                <span>{item.label}</span>
              </a>
            ))}
          </nav>

          <Separator className="my-4" />

          <LogoutButton />
        </aside>

        <main className="flex-1 bg-gray-50">{children}</main>
      </div>
    </ProtectedRoute>
  );
}