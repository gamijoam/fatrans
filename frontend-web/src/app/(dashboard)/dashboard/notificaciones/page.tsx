'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { Bell, CheckCheck, Check, Loader2, ChevronLeft, ChevronRight } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import {
  parseNotificacionesResponse,
  estiloPorPrioridad,
  formatearTiempoRelativo,
  type NotificacionApi,
} from '@/lib/utils/parse-notificaciones-response';

/**
 * Página dedicada con histórico paginado de notificaciones (issue #214 PR-B).
 *
 * Diferencias con el dropdown del Bell:
 * - Paginación completa (20 por página, navegación con prev/next).
 * - Filtro: todas / solo no leídas.
 * - Click en notificación con link la marca leída y navega.
 */

const PAGE_SIZE = 20;

export default function NotificacionesPage() {
  const [items, setItems] = useState<NotificacionApi[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(1);
  const [noLeidas, setNoLeidas] = useState(0);
  const [filtro, setFiltro] = useState<'todas' | 'no-leidas'>('todas');

  const cargar = useCallback(async () => {
    setLoading(true);
    try {
      const qs = new URLSearchParams({
        page: String(page),
        size: String(PAGE_SIZE),
        soloNoLeidas: filtro === 'no-leidas' ? 'true' : 'false',
      });
      const res = await fetch(`/api/notificaciones?${qs}`);
      if (res.ok) {
        const data = parseNotificacionesResponse(await res.json());
        setItems(data.notificaciones);
        setTotalPaginas(Math.max(1, data.totalPaginas));
        setNoLeidas(data.noLeidas);
      } else {
        setItems([]);
      }
    } catch {
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [page, filtro]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const marcarLeida = async (id: string) => {
    try {
      await fetch(`/api/notificaciones/${id}/leida`, { method: 'PATCH' });
      setItems((prev) =>
        prev.map((n) => (n.id === id ? { ...n, leida: true } : n))
      );
      setNoLeidas((prev) => Math.max(0, prev - 1));
    } catch {
      cargar();
    }
  };

  const marcarTodas = async () => {
    try {
      await fetch('/api/notificaciones/marcar-todas-leidas', { method: 'POST' });
      cargar();
    } catch {
      cargar();
    }
  };

  return (
    <div className="p-4 lg:p-6 max-w-4xl mx-auto space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-[#0F2744]">Notificaciones</h1>
          <p className="text-sm text-slate-500 mt-1">
            {noLeidas > 0
              ? `Tienes ${noLeidas} ${noLeidas === 1 ? 'notificación' : 'notificaciones'} sin leer`
              : 'Estás al día — sin notificaciones pendientes'}
          </p>
        </div>
        {noLeidas > 0 && (
          <button
            onClick={marcarTodas}
            className="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium text-[#16A34A] bg-emerald-50 hover:bg-emerald-100 rounded-lg transition-colors"
          >
            <CheckCheck className="w-4 h-4" />
            Marcar todas como leídas
          </button>
        )}
      </div>

      {/* Filtros */}
      <div role="tablist" className="inline-flex bg-slate-100 rounded-lg p-1 text-sm">
        <FiltroTab
          activo={filtro === 'todas'}
          onClick={() => {
            setPage(0);
            setFiltro('todas');
          }}
          label="Todas"
        />
        <FiltroTab
          activo={filtro === 'no-leidas'}
          onClick={() => {
            setPage(0);
            setFiltro('no-leidas');
          }}
          label="No leídas"
          badge={noLeidas > 0 ? String(noLeidas) : undefined}
        />
      </div>

      {/* Lista */}
      <Card className="border-slate-200">
        <CardContent className="p-0">
          {loading ? (
            <div className="p-12 text-center">
              <Loader2 className="w-6 h-6 animate-spin text-[#16A34A] mx-auto" />
            </div>
          ) : items.length === 0 ? (
            <div className="p-12 text-center">
              <Bell className="w-12 h-12 text-slate-300 mx-auto mb-3" />
              <p className="text-sm text-slate-500">
                {filtro === 'no-leidas'
                  ? 'No tienes notificaciones sin leer'
                  : 'Aún no tienes notificaciones'}
              </p>
            </div>
          ) : (
            items.map((n) => (
              <NotificacionRow key={n.id} notif={n} onMarcarLeida={() => marcarLeida(n.id)} />
            ))
          )}
        </CardContent>
      </Card>

      {/* Paginación */}
      {totalPaginas > 1 && (
        <div className="flex items-center justify-between">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            className="inline-flex items-center gap-1 px-3 py-2 text-sm text-slate-600 hover:text-slate-900 disabled:opacity-40 disabled:cursor-not-allowed"
          >
            <ChevronLeft className="w-4 h-4" /> Anterior
          </button>
          <span className="text-sm text-slate-500">
            Página {page + 1} de {totalPaginas}
          </span>
          <button
            onClick={() => setPage((p) => Math.min(totalPaginas - 1, p + 1))}
            disabled={page >= totalPaginas - 1}
            className="inline-flex items-center gap-1 px-3 py-2 text-sm text-slate-600 hover:text-slate-900 disabled:opacity-40 disabled:cursor-not-allowed"
          >
            Siguiente <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      )}
    </div>
  );
}

function FiltroTab({
  activo,
  onClick,
  label,
  badge,
}: {
  activo: boolean;
  onClick: () => void;
  label: string;
  badge?: string;
}) {
  return (
    <button
      role="tab"
      aria-selected={activo}
      onClick={onClick}
      className={`inline-flex items-center gap-2 px-4 py-1.5 rounded-md transition-colors ${
        activo ? 'bg-white text-[#0F2744] shadow-sm font-medium' : 'text-slate-600 hover:text-slate-900'
      }`}
    >
      {label}
      {badge && (
        <span className="bg-red-500 text-white text-[10px] font-bold rounded-full min-w-[1rem] h-4 px-1 flex items-center justify-center">
          {badge}
        </span>
      )}
    </button>
  );
}

function NotificacionRow({
  notif,
  onMarcarLeida,
}: {
  notif: NotificacionApi;
  onMarcarLeida: () => void;
}) {
  const estilo = estiloPorPrioridad(notif.prioridad as string);
  const dotColor = {
    urgent: 'bg-red-500',
    normal: 'bg-blue-500',
    low: 'bg-slate-400',
  }[estilo];

  const wrapperClass = `block px-5 py-4 border-b border-slate-100 last:border-0 ${
    notif.leida ? 'opacity-60' : 'bg-blue-50/30'
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
          className={`text-sm ${notif.leida ? 'text-slate-700' : 'font-semibold text-[#0F2744]'}`}
        >
          {notif.titulo}
        </p>
        <p className="text-sm text-slate-600 mt-1">{notif.mensaje}</p>
        <p className="text-xs text-slate-400 mt-2">
          {formatearTiempoRelativo(notif.createdAt)}
        </p>
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
          className="flex-shrink-0 text-slate-400 hover:text-emerald-600 transition-colors p-2"
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
