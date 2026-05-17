'use client';

import { useState, useRef, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuthStore } from '@/stores/auth-store';
import { ProtectedRoute } from '@/components/shared/protected-route';
import { LogoutButton } from '@/components/shared/logout-button';
import { IdleTimeoutWatcher } from '@/components/shared/idle-timeout-watcher';
import {
  LayoutDashboard,
  Wallet,
  CreditCard,
  FileText,
  User,
  Users,
  Menu,
  X,
  ChevronRight,
  Bell,
  Settings,
  LogOut,
  Shield,
} from 'lucide-react';

const menuItems = [
  { icon: LayoutDashboard, label: 'Inicio', href: '/dashboard' },
  { icon: Wallet, label: 'Cuentas', href: '/dashboard/cuentas' },
  { icon: CreditCard, label: 'Créditos', href: '/dashboard/creditos' },
  { icon: Users, label: 'Beneficiarios', href: '/dashboard/beneficiarios' },
  { icon: Shield, label: 'KYC', href: '/dashboard/kyc' },
  { icon: FileText, label: 'Documentos', href: '/dashboard/documentos' },
  { icon: User, label: 'Mi Perfil', href: '/perfil' },
];

function AvatarSocio({ name, size = 'md' }: { name: string; size?: 'sm' | 'md' | 'lg' }) {
  const sizes = {
    sm: 'w-8 h-8 text-xs',
    md: 'w-10 h-10 text-sm',
    lg: 'w-14 h-14 text-lg',
  };

  return (
    <div className={`${sizes[size]} rounded-full bg-gradient-to-br from-[#0F2744] to-[#1a4a7a] flex items-center justify-center text-white font-semibold shadow-md`}>
      {name?.charAt(0)?.toUpperCase() || 'U'}
    </div>
  );
}

export function SocioShell({ children }: { children: React.ReactNode }) {
  const user = useAuthStore((state) => state.user);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const pathname = usePathname();
  const profileRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (profileRef.current && !profileRef.current.contains(event.target as Node)) {
        setProfileOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const today = new Date().toLocaleDateString('es-VE', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
  });

  return (
    <ProtectedRoute allowedRoles={['SOCIO', 'ADMIN', 'SUPER_ADMIN']}>
      {/* Issue #221: auto-logout por inactividad (banking standard) */}
      <IdleTimeoutWatcher idleMinutes={10} warningSeconds={60} />
      <div className="flex min-h-screen bg-slate-100">
        {/* Desktop Sidebar - Minimal & Clean */}
        <aside className="hidden lg:flex w-64 bg-white flex-col fixed h-full z-40 border-r border-slate-200">
          {/* Logo */}
          <div className="p-6 border-b border-slate-100">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-[#0F2744] to-[#1a4a7a] flex items-center justify-center shadow-md">
                <span className="text-white font-bold text-sm">FA</span>
              </div>
              <div>
                <h1 className="text-lg font-bold text-[#0F2744] tracking-tight">FATRANS</h1>
                <p className="text-[10px] text-slate-500 uppercase tracking-widest">Mi Cuenta</p>
              </div>
            </div>
          </div>

          {/* User Card */}
          {user && (
            <div className="p-4 mx-3 mt-4">
              <div className="flex items-center gap-3 p-3 bg-slate-50 rounded-xl">
                <AvatarSocio name={user.nombreCompleto || 'Usuario'} size="sm" />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold text-slate-900 truncate">{user.nombreCompleto || 'Usuario'}</p>
                  <p className="text-xs text-slate-500 truncate">{user.rol}</p>
                </div>
              </div>
            </div>
          )}

          {/* Navigation - Clean links without boxes */}
          <nav className="flex-1 px-3 py-2 space-y-0.5">
            {menuItems.map((item) => {
              const isActive = pathname === item.href || (item.href !== '/dashboard' && pathname.startsWith(item.href));
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                    isActive
                      ? 'bg-[#0F2744]/5 text-[#0F2744] font-semibold'
                      : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
                  }`}
                >
                  <item.icon className={`w-5 h-5 ${isActive ? 'text-[#16A34A]' : 'text-slate-400'}`} />
                  <span>{item.label}</span>
                  {isActive && <ChevronRight className="w-4 h-4 ml-auto text-[#16A34A]" />}
                </Link>
              );
            })}
          </nav>

          {/* Bottom Actions */}
          <div className="p-3 border-t border-slate-100">
            <LogoutButton />
          </div>
        </aside>

        {/* Mobile Menu Overlay */}
        {mobileMenuOpen && (
          <div className="fixed inset-0 z-50 lg:hidden">
            <div className="fixed inset-0 bg-black/50" onClick={() => setMobileMenuOpen(false)} />
            <aside className="fixed left-0 top-0 bottom-0 w-80 bg-white p-4 flex flex-col">
              <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-[#0F2744] to-[#1a4a7a] flex items-center justify-center">
                    <span className="text-white font-bold text-sm">FA</span>
                  </div>
                  <div>
                    <h1 className="text-lg font-bold text-[#0F2744]">FATRANS</h1>
                    <p className="text-[10px] text-slate-500 uppercase">Mi Cuenta</p>
                  </div>
                </div>
                <button onClick={() => setMobileMenuOpen(false)} className="p-2 hover:bg-slate-100 rounded-lg">
                  <X className="w-5 h-5 text-slate-600" />
                </button>
              </div>

              {user && (
                <div className="mb-4 p-3 bg-slate-50 rounded-xl">
                  <div className="flex items-center gap-3">
                    <AvatarSocio name={user.nombreCompleto || 'Usuario'} size="sm" />
                    <div>
                      <p className="text-sm font-semibold">{user.nombreCompleto}</p>
                      <p className="text-xs text-slate-500">{user.rol}</p>
                    </div>
                  </div>
                </div>
              )}

              <nav className="flex-1 space-y-0.5">
                {menuItems.map((item) => {
                  const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
                  return (
                    <Link
                      key={item.href}
                      href={item.href}
                      onClick={() => setMobileMenuOpen(false)}
                      className={`flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium ${
                        isActive ? 'bg-[#0F2744]/5 text-[#0F2744]' : 'text-slate-600 hover:bg-slate-50'
                      }`}
                    >
                      <item.icon className="w-5 h-5" />
                      {item.label}
                    </Link>
                  );
                })}
              </nav>

              <div className="mt-4 pt-4 border-t border-slate-100">
                <LogoutButton />
              </div>
            </aside>
          </div>
        )}

        {/* Main Content */}
        <div className="flex-1 lg:ml-64 flex flex-col min-h-screen">
          {/* Top Navbar - Clean & Minimal */}
          <header className="bg-white border-b border-slate-200 sticky top-0 z-30">
            <div className="flex items-center justify-between px-4 lg:px-8 h-16">
              {/* Left: Hamburger (mobile only) + Title & Date */}
              <div className="flex items-center gap-3">
                {/* Botón hamburguesa: abre el side drawer en mobile/tablet.
                    En desktop (lg+) el sidebar fijo cubre la navegación, por eso
                    se oculta con lg:hidden — no queremos dos navs visibles. */}
                <button
                  type="button"
                  onClick={() => setMobileMenuOpen(true)}
                  aria-label="Abrir menú"
                  className="lg:hidden p-2 -ml-2 rounded-lg hover:bg-slate-100 text-slate-700"
                >
                  <Menu className="w-5 h-5" />
                </button>
                <div>
                  <h2 className="text-lg font-bold text-[#0F2744] capitalize">
                    {pathname === '/dashboard' ? 'Inicio' :
                     pathname === '/dashboard/cuentas' ? 'Mis Cuentas' :
                     pathname === '/dashboard/creditos' ? 'Créditos' :
                     pathname === '/dashboard/unidad' ? 'Mi Unidad' :
                     pathname === '/dashboard/beneficiarios' ? 'Beneficiarios' :
                     pathname === '/dashboard/documentos' ? 'Documentos' :
                     pathname === '/dashboard/kyc' ? 'Verificación KYC' :
                     pathname === '/perfil' ? 'Mi Perfil' : 'Fatrans'}
                  </h2>
                  <p className="text-xs text-slate-500 capitalize">{today}</p>
                </div>
              </div>

              {/* Right: Profile */}
              <div ref={profileRef} className="relative">
                <button
                  onClick={() => setProfileOpen(!profileOpen)}
                  className="flex items-center gap-3 p-1.5 hover:bg-slate-100 rounded-xl transition-colors"
                >
                  <AvatarSocio name={user?.nombreCompleto || 'U'} size="sm" />
                  <ChevronRight className={`w-4 h-4 text-slate-400 hidden sm:block transition-transform ${profileOpen ? 'rotate-90' : ''}`} />
                </button>

                {profileOpen && (
                  <div className="absolute right-0 mt-2 w-64 bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
                    {user && (
                      <div className="p-4 border-b border-slate-100">
                        <div className="flex items-center gap-3">
                          <AvatarSocio name={user.nombreCompleto || 'U'} size="md" />
                          <div>
                            <p className="font-semibold text-slate-900">{user.nombreCompleto}</p>
                            <p className="text-xs text-slate-500">{user.correoElectronico}</p>
                          </div>
                        </div>
                      </div>
                    )}
                    <div className="py-2">
                      <Link href="/perfil" className="flex items-center gap-3 px-4 py-3 text-sm text-slate-700 hover:bg-slate-50">
                        <User className="w-4 h-4" />
                        Mi Perfil
                      </Link>
                      <Link href="/dashboard/documentos" className="flex items-center gap-3 px-4 py-3 text-sm text-slate-700 hover:bg-slate-50">
                        <FileText className="w-4 h-4" />
                        Documentos
                      </Link>
                    </div>
                    <div className="py-2 border-t border-slate-100">
                      <LogoutButton />
                    </div>
                  </div>
                )}
              </div>
            </div>
          </header>

          {/* Page Content */}
          <main className="flex-1 p-4 lg:p-8">{children}</main>
        </div>
      </div>
    </ProtectedRoute>
  );
}
