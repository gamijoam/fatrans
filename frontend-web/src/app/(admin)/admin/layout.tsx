'use client';

import { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuthStore } from '@/stores/auth-store';
import { ProtectedRoute } from '@/components/shared/protected-route';
import { LogoutButton } from '@/components/shared/logout-button';
import {
  LayoutDashboard,
  Users,
  FileText,
  CreditCard,
  Shield,
  BarChart3,
  Settings,
  Menu,
  X,
  User,
} from 'lucide-react';
import { Separator } from '@/components/ui/separator';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

const adminMenuItems = [
  { icon: LayoutDashboard, label: 'Dashboard', href: '/admin' },
  { icon: Users, label: 'Socios', href: '/admin/socios' },
  { icon: FileText, label: 'Solicitudes', href: '/admin/solicitudes' },
  { icon: CreditCard, label: 'Créditos', href: '/admin/creditos' },
  { icon: Shield, label: 'KYC', href: '/admin/kyc' },
  { icon: BarChart3, label: 'Reportes', href: '/admin/reportes' },
  { icon: Settings, label: 'Configuración', href: '/admin/configuracion' },
];

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const user = useAuthStore((state) => state.user);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const pathname = usePathname();

  return (
    <ProtectedRoute allowedRoles={['ADMIN', 'SUPER_ADMIN', 'CAJERO', 'ANALISTA_KYC']}>
      <div className="flex min-h-screen">
        <aside className="hidden md:flex w-64 bg-slate-900 text-white p-4 flex-col">
          <div className="mb-6">
            <div className="flex items-center gap-2 mb-1">
              <div className="w-8 h-8 rounded-lg bg-green-600 flex items-center justify-center">
                <span className="text-white font-bold text-sm">FA</span>
              </div>
              <h2 className="text-lg font-bold text-green-500">FATRANS</h2>
            </div>
            <p className="text-xs text-slate-400">Panel Admin</p>
          </div>

          {user && (
            <div className="mb-4 p-3 bg-slate-800 rounded-lg border border-slate-700">
              <p className="font-medium text-sm text-white truncate">{user.nombreCompleto}</p>
              <div className="flex items-center gap-2 mt-1">
                <Badge variant="outline" className="text-xs border-slate-600 text-slate-300">
                  {user.rol}
                </Badge>
              </div>
            </div>
          )}

          <nav className="flex-1 space-y-1">
            {adminMenuItems.map((item) => {
              const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`flex items-center gap-3 px-3 py-2 text-sm rounded-md transition-colors ${
                    isActive
                      ? 'bg-slate-800 text-green-400 border-l-4 border-green-500'
                      : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                  }`}
                >
                  <item.icon className="h-4 w-4" />
                  <span>{item.label}</span>
                </Link>
              );
            })}
          </nav>

          <Separator className="my-4 border-slate-700" />

          <LogoutButton />
        </aside>

        {mobileMenuOpen && (
          <div className="fixed inset-0 z-50 md:hidden">
            <div className="fixed inset-0 bg-black/50" onClick={() => setMobileMenuOpen(false)} />
            <aside className="fixed left-0 top-0 bottom-0 w-64 bg-slate-900 text-white p-4 flex flex-col">
              <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-lg bg-green-600 flex items-center justify-center">
                    <span className="text-white font-bold text-sm">FA</span>
                  </div>
                  <h2 className="text-lg font-bold text-green-500">FATRANS</h2>
                </div>
                <button
                  onClick={() => setMobileMenuOpen(false)}
                  className="p-2 text-slate-300 hover:text-white"
                >
                  <X className="h-5 w-5" />
                </button>
              </div>

              {user && (
                <div className="mb-4 p-3 bg-slate-800 rounded-lg border border-slate-700">
                  <p className="font-medium text-sm text-white truncate">{user.nombreCompleto}</p>
                  <div className="flex items-center gap-2 mt-1">
                    <Badge variant="outline" className="text-xs border-slate-600 text-slate-300">
                      {user.rol}
                    </Badge>
                  </div>
                </div>
              )}

              <nav className="flex-1 space-y-1">
                {adminMenuItems.map((item) => {
                  const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
                  return (
                    <Link
                      key={item.href}
                      href={item.href}
                      onClick={() => setMobileMenuOpen(false)}
                      className={`flex items-center gap-3 px-3 py-2.5 text-sm rounded-md transition-colors min-h-[44px] ${
                        isActive
                          ? 'bg-slate-800 text-green-400 border-l-4 border-green-500'
                          : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                      }`}
                    >
                      <item.icon className="h-4 w-4" />
                      <span>{item.label}</span>
                    </Link>
                  );
                })}
              </nav>

              <Separator className="my-4 border-slate-700" />

              <LogoutButton />
            </aside>
          </div>
        )}

        <main className="flex-1 bg-slate-50">
          <header className="md:hidden flex items-center justify-between p-4 bg-slate-900 text-white">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-green-600 flex items-center justify-center">
                <span className="text-white font-bold text-sm">FA</span>
              </div>
              <h2 className="text-lg font-bold text-green-500">FATRANS</h2>
            </div>
            <Button
              variant="ghost"
              size="icon"
              onClick={() => setMobileMenuOpen(true)}
              className="text-white hover:bg-slate-800 min-w-[44px] min-h-[44px]"
            >
              <Menu className="h-5 w-5" />
            </Button>
          </header>

          {children}
        </main>
      </div>
    </ProtectedRoute>
  );
}