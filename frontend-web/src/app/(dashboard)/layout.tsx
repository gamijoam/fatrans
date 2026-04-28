'use client';

import { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuthStore } from '@/stores/auth-store';
import { ProtectedRoute } from '@/components/shared/protected-route';
import { LogoutButton } from '@/components/shared/logout-button';
import { LayoutDashboard, Wallet, CreditCard, FileText, User, Menu, X, Users } from 'lucide-react';
import { Separator } from '@/components/ui/separator';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

const menuItems = [
  { icon: LayoutDashboard, label: 'Dashboard', href: '/dashboard' },
  { icon: Wallet, label: 'Cuentas', href: '/dashboard/cuentas' },
  { icon: CreditCard, label: 'Créditos', href: '/dashboard/creditos' },
  { icon: Users, label: 'Beneficiarios', href: '/dashboard/beneficiarios' },
  { icon: FileText, label: 'Documentos', href: '/dashboard/documentos' },
  { icon: User, label: 'Mi Perfil', href: '/perfil' },
];

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const user = useAuthStore((state) => state.user);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const pathname = usePathname();

  return (
    <ProtectedRoute allowedRoles={['SOCIO', 'ADMIN', 'SUPER_ADMIN']}>
      <div className="flex min-h-screen">
        {/* Desktop Sidebar */}
        <aside className="hidden md:flex w-64 bg-white border-r border-gray-200 p-4 flex-col">
          <div className="mb-6">
            <div className="flex items-center gap-2 mb-1">
              <div className="w-8 h-8 rounded-lg bg-green-600 flex items-center justify-center">
                <span className="text-white font-bold text-sm">FA</span>
              </div>
              <h2 className="text-lg font-bold text-green-600">FATRANS</h2>
            </div>
            <p className="text-xs text-gray-500">Fondo de Ahorro</p>
          </div>

          {user && (
            <div className="mb-4 p-3 bg-gray-50 rounded-lg border border-gray-100">
              <p className="font-medium text-sm text-gray-900 truncate">{user.nombreCompleto}</p>
              <div className="flex items-center gap-2 mt-1">
                <Badge variant="outline" className="text-xs">
                  {user.rol}
                </Badge>
              </div>
            </div>
          )}

          <nav className="flex-1 space-y-1">
            {menuItems.map((item) => {
              const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`flex items-center gap-3 px-3 py-2.5 text-sm rounded-md transition-colors min-h-[44px] ${
                    isActive
                      ? 'bg-green-50 text-green-700 border-l-4 border-green-600'
                      : 'text-gray-700 hover:bg-gray-100'
                  }`}
                >
                  <item.icon className="h-4 w-4" />
                  <span>{item.label}</span>
                </Link>
              );
            })}
          </nav>

          <Separator className="my-4" />

          <LogoutButton />
        </aside>

        {/* Mobile Sidebar */}
        {mobileMenuOpen && (
          <div className="fixed inset-0 z-50 md:hidden">
            <div className="fixed inset-0 bg-black/50" onClick={() => setMobileMenuOpen(false)} />
            <aside className="fixed left-0 top-0 bottom-0 w-64 bg-white p-4 flex flex-col">
              <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-lg bg-green-600 flex items-center justify-center">
                    <span className="text-white font-bold text-sm">FA</span>
                  </div>
                  <h2 className="text-lg font-bold text-green-600">FATRANS</h2>
                </div>
                <button onClick={() => setMobileMenuOpen(false)} className="p-2">
                  <X className="h-5 w-5" />
                </button>
              </div>

              {user && (
                <div className="mb-4 p-3 bg-gray-50 rounded-lg border border-gray-100">
                  <p className="font-medium text-sm text-gray-900 truncate">{user.nombreCompleto}</p>
                  <div className="flex items-center gap-2 mt-1">
                    <Badge variant="outline" className="text-xs">
                      {user.rol}
                    </Badge>
                  </div>
                </div>
              )}

              <nav className="flex-1 space-y-1">
                {menuItems.map((item) => {
                  const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
                  return (
                    <Link
                      key={item.href}
                      href={item.href}
                      onClick={() => setMobileMenuOpen(false)}
                      className={`flex items-center gap-3 px-3 py-2.5 text-sm rounded-md transition-colors min-h-[44px] ${
                        isActive
                          ? 'bg-green-50 text-green-700 border-l-4 border-green-600'
                          : 'text-gray-700 hover:bg-gray-100'
                      }`}
                    >
                      <item.icon className="h-4 w-4" />
                      <span>{item.label}</span>
                    </Link>
                  );
                })}
              </nav>

              <Separator className="my-4" />

              <LogoutButton />
            </aside>
          </div>
        )}

        {/* Main Content */}
        <main className="flex-1 bg-gray-50">
          {/* Mobile Header */}
          <header className="md:hidden flex items-center justify-between p-4 bg-white border-b border-gray-200">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-green-600 flex items-center justify-center">
                <span className="text-white font-bold text-sm">FA</span>
              </div>
              <h2 className="text-lg font-bold text-green-600">FATRANS</h2>
            </div>
            <Button variant="ghost" size="icon" onClick={() => setMobileMenuOpen(true)}>
              <Menu className="h-5 w-5" />
            </Button>
          </header>

          {children}
        </main>
      </div>
    </ProtectedRoute>
  );
}