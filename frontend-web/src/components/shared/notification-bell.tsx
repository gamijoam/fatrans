'use client';

import { useEffect, useState, useCallback, useRef } from 'react';
import Link from 'next/link';
import { Bell, Check, CheckCheck } from 'lucide-react';
import { useAuthStore } from '@/stores/auth-store';
import {
  parseNotificacionesResponse,
  formatearBadgeCount,
  estiloPorPrioridad,
  formatearTiempoRelativo,
  type NotificacionApi,
} from '@/lib/utils/parse-notificaciones-response';

/**
 * Campana de notificaciones con badge + dropdown (issue #214 PR-B).
 *
 * <p>Polling cada 30s al endpoint ligero `/api/notificaciones/count` para
 * el badge. El dropdown completo (lista de últimas 5) solo se carga al
 * abrir, para no transferir datos innecesarios.</p>
 *
 * <p>Diseño "anti-mock": antes del PR-A había `<Bell />` importado pero
 * nunca renderizado, y `mockNotifications` hardcoded en admin-shell.
 * Este componente reemplaza ambos.</p>
 */

const POLLING_INTERVAL_MS = 30_000;
const NOTIFICACIONES_EN_DROPDOWN = 5;

export function NotificationBell() {
  const user = useAuthStore((state) => state.user);
  const [count, setCount] = useState(0);
  const [open, setOpen] = useState(false);
  const [notificaciones, setNotificaciones] = useState<NotificacionApi[]>([]);
  const [loadingList, setLoadingList] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  // === Polling del badge ===
  const fetchCount = useCallback(async () => {
    if (!user) return;
    try {
      const res = await fetch('/api/notificaciones/count', { cache: 'no-store' });
      if (res.ok) {
        const data = await res.json();
        setCount(Number(data?.noLeidas) || 0);
      }
    } catch {
      // Silent — el polling no debe ensuciar consola con errores transitorios
    }
  }, [user]);

  useEffect(() => {
    if (!user) return;
    fetchCount();
    const interval = setInterval(fetchCount, POLLING_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [user, fetchCount]);

  // === Cargar lista al abrir el dropdown ===
  const cargarLista = useCallback(async () => {
    setLoadingList(true);
    try {
      const res = await fetch(
        `/api/notificaciones?page=0&size=${NOTIFICACIONES_EN_DROPDOWN}`
      );
      if (res.ok) {
        const data = parseNotificacionesResponse(await res.json());
        setNotificaciones(data.notificaciones);
        setCount(data.noLeidas);
      }
    } catch {
      // Empty state se ve igual al de "sin notificaciones"
    } finally {
      setLoadingList(false);
    }
  }, []);

  // Cerrar al click fuera
  useEffect(() => {
    if (!open) return;
    const handler = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [open]);

  const toggleDropdown = () => {
    const willOpen = !open;
    setOpen(willOpen);
    if (willOpen) cargarLista();
  };

  // === Acciones ===
  const marcarLeida = async (id: string) => {
    try {
      await fetch(`/api/notificaciones/${id}/leida`, { method: 'PATCH' });
      // Optimistic update local
      setNotificaciones((prev) =>
        prev.map((n) => (n.id === id ? { ...n, leida: true } : n))
      );
      // Refrescar contador con el real
      fetchCount();
    } catch {
      // Si falla, recargar para recuperar estado real
      cargarLista();
    }
  };

  const marcarTodas = async () => {
    try {
      await fetch('/api/notificaciones/marcar-todas-leidas', { method: 'POST' });
      setNotificaciones((prev) => prev.map((n) => ({ ...n, leida: true })));
      setCount(0);
    } catch {
      cargarLista();
    }
  };

  if (!user) return null;

  const badge = formatearBadgeCount(count);

  return (
    <div className="relative" ref={containerRef}>
      <button
        onClick={toggleDropdown}
        aria-label={
          badge
            ? `Notificaciones (${count} no leídas)`
            : 'Notificaciones'
        }
        data-testid="notification-bell-trigger"
        className="relative p-2 text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-lg transition-colors"
      >
        <Bell className="w-5 h-5" />
        {badge && (
          <span
            data-testid="notification-badge"
            className="absolute -top-0.5 -right-0.5 min-w-[1.25rem] h-5 px-1 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center"
          >
            {badge}
          </span>
        )}
      </button>

      {open && (
        <div
          role="menu"
          data-testid="notification-dropdown"
          className="absolute right-0 mt-2 w-80 sm:w-96 bg-white rounded-2xl shadow-xl border border-slate-200 z-50 overflow-hidden"
        >
          <div className="flex items-center justify-between px-4 py-3 border-b border-slate-100">
            <h3 className="text-sm font-semibold text-[#0F2744]">Notificaciones</h3>
            {count > 0 && (
              <button
                onClick={marcarTodas}
                className="text-xs text-[#16A34A] font-medium hover:underline flex items-center gap-1"
              >
                <CheckCheck className="w-3 h-3" />
                Marcar todas
              </button>
            )}
          </div>

          <div className="max-h-96 overflow-y-auto">
            {loadingList ? (
              <div className="p-8 text-center text-sm text-slate-500">
                Cargando...
              </div>
            ) : notificaciones.length === 0 ? (
              <div className="p-8 text-center">
                <Bell className="w-10 h-10 text-slate-300 mx-auto mb-2" />
                <p className="text-sm text-slate-500">Sin notificaciones</p>
              </div>
            ) : (
              notificaciones.map((n) => (
                <NotificationItem
                  key={n.id}
                  notif={n}
                  onMarcarLeida={() => marcarLeida(n.id)}
                />
              ))
            )}
          </div>

          <div className="border-t border-slate-100 p-2">
            <Link
              href="/dashboard/notificaciones"
              onClick={() => setOpen(false)}
              className="block w-full text-center text-xs font-medium text-[#16A34A] hover:bg-slate-50 py-2 rounded-lg transition-colors"
            >
              Ver todas las notificaciones
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}

function NotificationItem({
  notif,
  onMarcarLeida,
}: {
  notif: NotificacionApi;
  onMarcarLeida: () => void;
}) {
  const estilo = estiloPorPrioridad(notif.prioridad as string);
  const tiempo = formatearTiempoRelativo(notif.createdAt);

  const dotColor = {
    urgent: 'bg-red-500',
    normal: 'bg-blue-500',
    low: 'bg-slate-400',
  }[estilo];

  const wrapperClass = `block px-4 py-3 border-b border-slate-50 last:border-0 ${
    notif.leida ? 'opacity-60' : 'bg-blue-50/40'
  } hover:bg-slate-50 transition-colors`;

  const contenido = (
    <div className="flex items-start gap-3">
      <span
        className={`flex-shrink-0 w-2 h-2 mt-2 rounded-full ${
          notif.leida ? 'bg-slate-300' : dotColor
        }`}
      />
      <div className="flex-1 min-w-0">
        <p
          className={`text-sm ${
            notif.leida ? 'text-slate-600' : 'font-semibold text-[#0F2744]'
          } truncate`}
        >
          {notif.titulo}
        </p>
        <p className="text-xs text-slate-500 line-clamp-2 mt-0.5">
          {notif.mensaje}
        </p>
        <p className="text-[11px] text-slate-400 mt-1">{tiempo}</p>
      </div>
      {!notif.leida && (
        <button
          onClick={(e) => {
            e.preventDefault();
            e.stopPropagation();
            onMarcarLeida();
          }}
          aria-label="Marcar como leída"
          title="Marcar como leída"
          className="flex-shrink-0 text-slate-400 hover:text-emerald-600 transition-colors p-1"
        >
          <Check className="w-4 h-4" />
        </button>
      )}
    </div>
  );

  if (notif.linkAccion) {
    return (
      <Link href={notif.linkAccion} className={wrapperClass}>
        {contenido}
      </Link>
    );
  }
  return <div className={wrapperClass}>{contenido}</div>;
}
