'use client';

import { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { ProtectedRoute } from '@/components/shared/protected-route';
import { LogoutButton } from '@/components/shared/logout-button';
import { Shield, Users, FileText, BarChart3, Settings, CreditCard, ClipboardList, Menu, X } from 'lucide-react';
import { Separator } from '@/components/ui/separator';
import { Button } from '@/components/ui/button';

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
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const pathname = usePathname();

  return (
    <ProtectedRoute allowedRoles={['ADMIN', 'ADMINISTRADOR', 'GESTOR', 'SUPER_ADMIN']}>
      <div className="flex min-h-screen">
        <aside className={`${mobileMenuOpen ? 'block' : 'hidden'} fixed inset-0 z-50 lg:relative lg:inset-auto lg:w-64 lg:block w-64 bg-gray-900 text-white p-4 flex flex-col`}>
          <div className="mb-6 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Shield className="h-6 w-6 text-green-400" />
              <h2 className="text-lg font-bold text-white">Admin</h2>
            </div>
            <Button
              variant="ghost"
              size="sm"
              className="lg:hidden text-white hover:bg-gray-800"
              onClick={() => setMobileMenuOpen(false)}
            >
              <X className="h-5 w-5" />
            </Button>
          </div>

          <nav className="flex-1 space-y-1">
            {adminMenuItems.map((item) => {
              const isActive = pathname === item.href;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={() => setMobileMenuOpen(false)}
                  className={`flex items-center gap-3 px-3 py-2 text-sm rounded-md transition-colors ${
                    isActive
                      ? 'bg-gray-800 text-white'
                      : 'text-gray-300 hover:bg-gray-800 hover:text-white'
                  }`}
                >
                  <item.icon className="h-4 w-4" />
                  <span>{item.label}</span>
                </Link>
              );
            })}
          </nav>

          <Separator className="my-4 bg-gray-700" />

          <LogoutButton />
        </aside>

        {mobileMenuOpen && (
          <div
            className="fixed inset-0 bg-black/50 z-40 lg:hidden"
            onClick={() => setMobileMenuOpen(false)}
          />
        )}

        <main className="flex-1 bg-gray-100">
          <div className="lg:hidden p-4">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setMobileMenuOpen(true)}
              className="bg-white"
            >
              <Menu className="h-5 w-5 mr-2" />
              Menú
            </Button>
          </div>
          {children}
        </main>
      </div>
    </ProtectedRoute>
  );
}
