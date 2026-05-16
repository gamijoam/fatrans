'use client';

import { useState, useRef, useEffect } from 'react';
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
  Search,
  Bell,
  ChevronDown,
  Menu,
  X,
  LogOut,
  User,
  Truck,
  ChevronRight,
  Activity,
  Users2,
} from 'lucide-react';
import { Badge } from '@/components/ui/badge';

const adminMenuItems = [
  { icon: LayoutDashboard, label: 'Tablero de Control', href: '/admin' },
  { icon: Users2, label: 'Usuarios', href: '/admin/usuarios' },
  { icon: Truck, label: 'Transportistas', href: '/admin/socios' },
  { icon: FileText, label: 'Solicitudes', href: '/admin/solicitudes' },
  { icon: CreditCard, label: 'Créditos', href: '/admin/creditos' },
  { icon: Shield, label: 'KYC', href: '/admin/kyc' },
  { icon: BarChart3, label: 'Reportes', href: '/admin/reportes' },
  { icon: Settings, label: 'Configuración', href: '/admin/configuracion' },
];

interface Notification {
  id: string;
  title: string;
  message: string;
  time: string;
  read: boolean;
}

const mockNotifications: Notification[] = [
  { id: '1', title: 'Nueva solicitud', message: 'Solicitud de crédito pendiente de revisión', time: 'Hace 5 min', read: false },
  { id: '2', title: 'KYC aprobado', message: 'Verificación de identidad completada', time: 'Hace 1 hora', read: false },
  { id: '3', title: 'Depósito recibido', message: 'Bs 5,000,000 depositados a cuenta', time: 'Hace 2 horas', read: true },
  { id: '4', title: 'Usuario bloqueado', message: '5 intentos fallidos de login', time: 'Hace 3 horas', read: true },
];

export function AdminShell({ children }: { children: React.ReactNode }) {
  const user = useAuthStore((state) => state.user);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchFocused, setSearchFocused] = useState(false);
  const pathname = usePathname();
  const profileRef = useRef<HTMLDivElement>(null);
  const notifRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (profileRef.current && !profileRef.current.contains(event.target as Node)) {
        setProfileOpen(false);
      }
      if (notifRef.current && !notifRef.current.contains(event.target as Node)) {
        setNotificationsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const unreadCount = mockNotifications.filter((n) => !n.read).length;

  return (
    <ProtectedRoute allowedRoles={['ADMIN', 'SUPER_ADMIN', 'CAJERO', 'ANALISTA_KYC']}>
      <div className="flex h-screen bg-slate-100">
        {/* Sidebar */}
        <aside className="hidden lg:flex w-64 bg-[#0F2744] text-white flex-col fixed h-full z-40">
          {/* Logo */}
          <div className="p-5 border-b border-white/10">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-[#16A34A] to-[#15803D] flex items-center justify-center shadow-lg shadow-[#16A34A]/30">
                <span className="text-white font-bold text-sm">FA</span>
              </div>
              <div>
                <h1 className="text-lg font-bold tracking-tight">FATRANS</h1>
                <p className="text-[10px] text-white/50 uppercase tracking-widest">Panel Admin</p>
              </div>
            </div>
          </div>

          {/* User Profile Mini */}
          {user && (
            <div className="p-4 mx-3 mt-4 bg-white/5 rounded-xl border border-white/10">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-full bg-gradient-to-br from-[#16A34A] to-emerald-600 flex items-center justify-center text-sm font-semibold">
                  {user.nombreCompleto?.charAt(0) || 'A'}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium truncate">{user.nombreCompleto || 'Admin'}</p>
                  <p className="text-[10px] text-white/50 truncate">{user.correoElectronico}</p>
                </div>
              </div>
              <Badge className="mt-2 bg-[#16A34A]/20 text-[#16A34A] border-0 text-[10px] px-2 py-0.5">
                {user.rol}
              </Badge>
            </div>
          )}

          {/* Navigation */}
          <nav className="flex-1 p-3 space-y-1 overflow-y-auto">
            {adminMenuItems.map((item) => {
              const isActive = pathname === item.href || (item.href !== '/admin' && pathname.startsWith(item.href));
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all group ${
                    isActive
                      ? 'bg-[#16A34A] text-white shadow-lg shadow-[#16A34A]/20'
                      : 'text-white/70 hover:bg-white/10 hover:text-white'
                  }`}
                >
                  <item.icon className={`w-5 h-5 ${isActive ? 'text-white' : 'text-white/50 group-hover:text-white'}`} />
                  <span>{item.label}</span>
                  {isActive && <ChevronRight className="w-4 h-4 ml-auto opacity-50" />}
                </Link>
              );
            })}
          </nav>

          {/* Logout — variant="ghost" para texto blanco sobre fondo oscuro del
              sidebar. El "default" usa text-gray-700 que se invisibilizaba
              contra #0F2744. */}
          <div className="p-3 border-t border-white/10">
            <LogoutButton variant="ghost" />
          </div>
        </aside>

        {/* Mobile Menu Overlay */}
        {mobileMenuOpen && (
          <div className="fixed inset-0 z-50 lg:hidden">
            <div className="fixed inset-0 bg-black/60" onClick={() => setMobileMenuOpen(false)} />
            <aside className="fixed left-0 top-0 bottom-0 w-72 bg-[#0F2744] text-white p-4 flex flex-col">
              <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-[#16A34A] to-[#15803D] flex items-center justify-center">
                    <span className="text-white font-bold text-sm">FA</span>
                  </div>
                  <h1 className="text-lg font-bold">FATRANS</h1>
                </div>
                <button onClick={() => setMobileMenuOpen(false)} className="p-2 hover:bg-white/10 rounded-lg">
                  <X className="w-5 h-5" />
                </button>
              </div>

              {user && (
                <div className="mb-4 p-3 bg-white/5 rounded-xl border border-white/10">
                  <p className="text-sm font-medium">{user.nombreCompleto}</p>
                  <Badge className="mt-1 bg-[#16A34A]/20 text-[#16A34A] border-0 text-[10px]">{user.rol}</Badge>
                </div>
              )}

              <nav className="flex-1 space-y-1 overflow-y-auto">
                {adminMenuItems.map((item) => {
                  const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
                  return (
                    <Link
                      key={item.href}
                      href={item.href}
                      onClick={() => setMobileMenuOpen(false)}
                      className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium ${
                        isActive ? 'bg-[#16A34A] text-white' : 'text-white/70 hover:bg-white/10'
                      }`}
                    >
                      <item.icon className="w-5 h-5" />
                      {item.label}
                    </Link>
                  );
                })}
              </nav>

              <div className="mt-4">
                <LogoutButton variant="ghost" />
              </div>
            </aside>
          </div>
        )}

        {/* Main Content */}
        <div className="flex-1 lg:ml-64 flex flex-col min-h-screen">
          {/* Top Navbar */}
          <header className="bg-white border-b border-slate-200 sticky top-0 z-30">
            <div className="flex items-center justify-between px-4 lg:px-6 h-16">
              {/* Mobile Menu Button */}
              <button
                onClick={() => setMobileMenuOpen(true)}
                className="lg:hidden p-2 hover:bg-slate-100 rounded-lg"
              >
                <Menu className="w-5 h-5 text-slate-600" />
              </button>

              {/* Search Bar */}
              <div className="hidden md:flex flex-1 max-w-xl relative">
                <div className={`relative w-full transition-all ${searchFocused ? 'ring-2 ring-[#16A34A]/20' : ''}`}>
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <input
                    type="text"
                    placeholder="Buscar transportistas, transacciones, solicitudes..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onFocus={() => setSearchFocused(true)}
                    onBlur={() => setSearchFocused(false)}
                    className="w-full h-10 pl-10 pr-4 bg-slate-50 border border-slate-200 rounded-xl text-sm placeholder:text-slate-400 focus:bg-white focus:border-[#16A34A] focus:outline-none transition-all"
                  />
                  {searchQuery && (
                    <button
                      onClick={() => setSearchQuery('')}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  )}
                </div>
              </div>

              {/* Right Actions */}
              <div className="flex items-center gap-2">
                {/* Notifications */}
                <div ref={notifRef} className="relative">
                  <button
                    onClick={() => setNotificationsOpen(!notificationsOpen)}
                    className="relative p-2 hover:bg-slate-100 rounded-lg transition-colors"
                  >
                    <Bell className="w-5 h-5 text-slate-600" />
                    {unreadCount > 0 && (
                      <span className="absolute top-1 right-1 w-4 h-4 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center">
                        {unreadCount}
                      </span>
                    )}
                  </button>

                  {notificationsOpen && (
                    <div className="absolute right-0 mt-2 w-80 bg-white rounded-xl shadow-xl shadow-slate-200/50 border border-slate-200 overflow-hidden">
                      <div className="px-4 py-3 border-b border-slate-100 flex items-center justify-between">
                        <h3 className="font-semibold text-sm text-slate-900">Notificaciones</h3>
                        <button className="text-xs text-[#16A34A] hover:underline">Marcar todas como leídas</button>
                      </div>
                      <div className="max-h-80 overflow-y-auto">
                        {mockNotifications.map((notif) => (
                          <div
                            key={notif.id}
                            className={`px-4 py-3 border-b border-slate-50 hover:bg-slate-50 cursor-pointer transition-colors ${
                              !notif.read ? 'bg-[#16A34A]/5' : ''
                            }`}
                          >
                            <div className="flex items-start gap-3">
                              {!notif.read && <div className="w-2 h-2 rounded-full bg-[#16A34A] mt-2 flex-shrink-0" />}
                              <div className={!notif.read ? '' : 'ml-5'}>
                                <p className="text-sm font-medium text-slate-900">{notif.title}</p>
                                <p className="text-xs text-slate-500 mt-0.5">{notif.message}</p>
                                <p className="text-[10px] text-slate-400 mt-1">{notif.time}</p>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                      <div className="px-4 py-3 bg-slate-50 border-t border-slate-100">
                        <Link href="/admin/notificaciones" className="text-xs text-[#16A34A] hover:underline font-medium">
                          Ver todas las notificaciones
                        </Link>
                      </div>
                    </div>
                  )}
                </div>

                {/* Profile Dropdown */}
                <div ref={profileRef} className="relative">
                  <button
                    onClick={() => setProfileOpen(!profileOpen)}
                    className="flex items-center gap-2 p-1.5 hover:bg-slate-100 rounded-lg transition-colors"
                  >
                    <div className="w-8 h-8 rounded-full bg-gradient-to-br from-[#16A34A] to-emerald-600 flex items-center justify-center text-white text-sm font-semibold">
                      {user?.nombreCompleto?.charAt(0) || 'A'}
                    </div>
                    <ChevronDown className="w-4 h-4 text-slate-400 hidden md:block" />
                  </button>

                  {profileOpen && (
                    <div className="absolute right-0 mt-2 w-56 bg-white rounded-xl shadow-xl shadow-slate-200/50 border border-slate-200 overflow-hidden">
                      {user && (
                        <div className="px-4 py-3 border-b border-slate-100">
                          <p className="font-medium text-sm text-slate-900">{user.nombreCompleto}</p>
                          <p className="text-xs text-slate-500">{user.correoElectronico}</p>
                        </div>
                      )}
                      <div className="py-1">
                        <Link href="/admin/perfil" className="flex items-center gap-3 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50">
                          <User className="w-4 h-4" />
                          Mi Perfil
                        </Link>
                        <Link href="/admin/configuracion" className="flex items-center gap-3 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50">
                          <Settings className="w-4 h-4" />
                          Configuración
                        </Link>
                      </div>
                      <div className="py-1 border-t border-slate-100">
                        <LogoutButton />
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </header>

          {/* Page Content */}
          <main className="flex-1 p-4 lg:p-6">{children}</main>
        </div>
      </div>
    </ProtectedRoute>
  );
}
