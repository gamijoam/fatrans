'use client';

import { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { ArrowDownToLine, ArrowUpFromLine, ArrowRight, FileDown, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

/**
 * Modal de detalle de un movimiento (issue #220).
 *
 * Muestra todos los campos del movimiento agrupados en secciones (datos
 * básicos, referencias, saldos, canal). El botón "Descargar comprobante"
 * llama al BFF `/api/cuentas/{n}/movimientos/{op}/comprobante` que a su
 * vez pide al backend regenerar el PDF on-demand (issue #220 PR-B).
 */

export interface MovimientoDetail {
  id: string;
  numeroOperacion: string;
  tipo: string;
  monto: number;
  saldoAnterior: number;
  saldoPosterior: number;
  descripcion: string | null;
  referencia: string | null;
  canalOrigen: string;
  estado: string;
  fechaMovimiento: string;
  fechaValor: string;
}

interface Props {
  movimiento: MovimientoDetail | null;
  moneda: string;
  /** Número de cuenta para construir la URL del comprobante. */
  numeroCuenta: string;
  onClose: () => void;
}

const formatMonto = (monto: number | null | undefined, moneda: string) => {
  if (monto == null) return '-';
  const simbolo = moneda === 'VES' ? 'Bs' : '$';
  return `${simbolo} ${monto.toLocaleString('es-VE', { minimumFractionDigits: 2 })}`;
};

const formatFechaCompleta = (fecha: string) => {
  return new Date(fecha).toLocaleString('es-VE', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
};

const formatFechaCorta = (fecha: string) => {
  return new Date(fecha).toLocaleDateString('es-VE', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

const etiquetaCanal = (canal: string): string => {
  switch (canal?.toUpperCase()) {
    case 'WEB':
      return 'Banca en línea';
    case 'MOVIL':
    case 'MÓVIL':
      return 'Aplicación móvil';
    case 'CAJA':
      return 'Caja presencial';
    case 'TRANSFERENCIA':
      return 'Transferencia';
    case 'API':
      return 'API';
    default:
      return canal || '—';
  }
};

const etiquetaEstado = (estado: string): { texto: string; clase: string } => {
  switch (estado?.toUpperCase()) {
    case 'COMPLETADO':
    case 'COMPLETADA':
    case 'EXITOSO':
      return { texto: 'Completado', clase: 'bg-green-100 text-green-800' };
    case 'PENDIENTE':
      return { texto: 'Pendiente', clase: 'bg-amber-100 text-amber-800' };
    case 'RECHAZADO':
    case 'FALLIDO':
      return { texto: 'Rechazado', clase: 'bg-red-100 text-red-800' };
    case 'REVERSADO':
      return { texto: 'Reversado', clase: 'bg-slate-200 text-slate-800' };
    default:
      return { texto: estado || '—', clase: 'bg-slate-100 text-slate-700' };
  }
};

export function MovimientoDetailModal({ movimiento, moneda, numeroCuenta, onClose }: Props) {
  const open = movimiento !== null;
  const esDeposito = movimiento?.tipo === 'DEPOSITO';
  const delta = movimiento ? movimiento.saldoPosterior - movimiento.saldoAnterior : 0;
  const estadoUi = movimiento ? etiquetaEstado(movimiento.estado) : null;
  const [descargando, setDescargando] = useState(false);

  const handleDescargar = async () => {
    if (!movimiento || descargando) return;
    setDescargando(true);
    try {
      const res = await fetch(
        `/api/cuentas/${encodeURIComponent(numeroCuenta)}/movimientos/${encodeURIComponent(movimiento.numeroOperacion)}/comprobante`,
      );
      if (!res.ok) {
        let msg = `No se pudo descargar el comprobante (HTTP ${res.status})`;
        try {
          const body = await res.json();
          if (body?.message) msg = body.message;
        } catch {
          /* respuesta sin body JSON */
        }
        toast.error(msg);
        return;
      }
      // Disparar descarga del blob via <a download>
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `Comprobante_${movimiento.numeroOperacion}.pdf`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      // Liberar memoria del object URL tras un pequeño delay para que el navegador
      // tenga tiempo de iniciar la descarga
      setTimeout(() => URL.revokeObjectURL(url), 1000);
      toast.success('Comprobante descargado');
    } catch (err) {
      console.error('Error descargando comprobante:', err);
      toast.error('Error de red al descargar el comprobante');
    } finally {
      setDescargando(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(o) => { if (!o) onClose(); }}>
      <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-[#0F2744]">
            {esDeposito ? (
              <ArrowDownToLine className="w-5 h-5 text-green-600" aria-hidden="true" />
            ) : (
              <ArrowUpFromLine className="w-5 h-5 text-red-600" aria-hidden="true" />
            )}
            Detalle del movimiento
          </DialogTitle>
          <DialogDescription>
            Operación{' '}
            <span className="font-mono text-xs text-slate-700">
              {movimiento?.numeroOperacion}
            </span>
          </DialogDescription>
        </DialogHeader>

        {movimiento && (
          <div className="space-y-5">
            {/* Sección: Datos básicos */}
            <section aria-labelledby="seccion-basicos">
              <h3 id="seccion-basicos" className="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-2">
                Datos básicos
              </h3>
              <dl className="space-y-2 text-sm">
                <DataRow label="Tipo">
                  <span
                    className={`inline-flex items-center gap-1 px-2 py-0.5 text-xs font-medium rounded-full ${
                      esDeposito ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    }`}
                  >
                    {esDeposito ? 'Depósito' : 'Retiro'}
                  </span>
                </DataRow>
                <DataRow label="Monto">
                  <span className={`text-lg font-bold ${esDeposito ? 'text-green-600' : 'text-red-600'}`}>
                    {esDeposito ? '+' : '−'} {formatMonto(movimiento.monto, moneda)}
                  </span>
                </DataRow>
                <DataRow label="Estado">
                  {estadoUi && (
                    <span className={`inline-block px-2 py-0.5 text-xs font-medium rounded-full ${estadoUi.clase}`}>
                      {estadoUi.texto}
                    </span>
                  )}
                </DataRow>
                <DataRow label="Fecha de movimiento">
                  <span className="text-slate-800">{formatFechaCompleta(movimiento.fechaMovimiento)}</span>
                </DataRow>
                {movimiento.fechaValor && movimiento.fechaValor !== movimiento.fechaMovimiento && (
                  <DataRow label="Fecha valor">
                    <span className="text-slate-800">{formatFechaCorta(movimiento.fechaValor)}</span>
                  </DataRow>
                )}
              </dl>
            </section>

            {/* Sección: Referencias */}
            <section aria-labelledby="seccion-referencias" className="pt-2 border-t border-slate-100">
              <h3 id="seccion-referencias" className="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-2">
                Referencias
              </h3>
              <dl className="space-y-2 text-sm">
                <DataRow label="Número de operación">
                  <span className="font-mono text-xs text-slate-800">{movimiento.numeroOperacion}</span>
                </DataRow>
                {movimiento.referencia && (
                  <DataRow label="Referencia externa">
                    <span className="font-mono text-xs text-slate-800">{movimiento.referencia}</span>
                  </DataRow>
                )}
                {movimiento.descripcion && (
                  <DataRow label="Descripción">
                    <span className="text-slate-800">{movimiento.descripcion}</span>
                  </DataRow>
                )}
              </dl>
            </section>

            {/* Sección: Saldos */}
            <section aria-labelledby="seccion-saldos" className="pt-2 border-t border-slate-100">
              <h3 id="seccion-saldos" className="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-2">
                Saldos
              </h3>
              <div className="flex items-center justify-between gap-3 rounded-lg bg-slate-50 px-4 py-3">
                <div className="text-center">
                  <p className="text-xs text-slate-500">Anterior</p>
                  <p className="text-sm font-semibold text-slate-800">
                    {formatMonto(movimiento.saldoAnterior, moneda)}
                  </p>
                </div>
                <ArrowRight className="w-4 h-4 text-slate-400 flex-shrink-0" aria-hidden="true" />
                <div className="text-center">
                  <p className="text-xs text-slate-500">Delta</p>
                  <p className={`text-sm font-semibold ${delta >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                    {delta >= 0 ? '+' : ''}{formatMonto(Math.abs(delta), moneda)}
                  </p>
                </div>
                <ArrowRight className="w-4 h-4 text-slate-400 flex-shrink-0" aria-hidden="true" />
                <div className="text-center">
                  <p className="text-xs text-slate-500">Posterior</p>
                  <p className="text-sm font-semibold text-slate-800">
                    {formatMonto(movimiento.saldoPosterior, moneda)}
                  </p>
                </div>
              </div>
            </section>

            {/* Sección: Canal */}
            <section aria-labelledby="seccion-canal" className="pt-2 border-t border-slate-100">
              <h3 id="seccion-canal" className="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-2">
                Canal
              </h3>
              <dl className="space-y-2 text-sm">
                <DataRow label="Origen">
                  <span className="text-slate-800">{etiquetaCanal(movimiento.canalOrigen)}</span>
                </DataRow>
              </dl>
            </section>

            {/* Acciones */}
            <div className="pt-4 border-t border-slate-100 flex flex-col sm:flex-row gap-2 sm:justify-end">
              <Button
                variant="outline"
                onClick={handleDescargar}
                disabled={descargando}
                aria-label={`Descargar comprobante PDF del movimiento ${movimiento.numeroOperacion}`}
                className="gap-2"
              >
                {descargando ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" aria-hidden="true" />
                    Generando...
                  </>
                ) : (
                  <>
                    <FileDown className="w-4 h-4" aria-hidden="true" />
                    Descargar comprobante
                  </>
                )}
              </Button>
              <Button onClick={onClose} className="bg-[#16A34A] hover:bg-green-700">
                Cerrar
              </Button>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}

/** Helper local: fila label / value alineada a la izquierda/derecha. */
function DataRow({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex flex-wrap items-baseline justify-between gap-2">
      <dt className="text-xs text-slate-500">{label}</dt>
      <dd className="text-right">{children}</dd>
    </div>
  );
}
