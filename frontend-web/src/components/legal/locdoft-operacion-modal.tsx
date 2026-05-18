'use client';

import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { AlertTriangle, ShieldCheck } from 'lucide-react';

/**
 * Modal de declaración jurada LOCDOFT para operaciones grandes (#218 PR-C).
 *
 * Aparece cuando el monto de un depósito o retiro supera el umbral
 * configurado. El usuario debe:
 *   1. Marcar el checkbox de declaración jurada (obligatorio)
 *   2. Opcionalmente describir el origen de los fondos
 *   3. Confirmar — el form padre vuelve a hacer la operación con
 *      `confirmaOrigenLicito=true` + `origenFondos` en el payload.
 *
 * Si el socio cierra el modal sin confirmar, la operación se cancela.
 */

export interface LocdoftOperacionModalProps {
  /** Si el modal está abierto. */
  open: boolean;
  /** Tipo de operación (afecta solo el texto). */
  tipoOperacion: 'deposito' | 'retiro';
  /** Monto que el usuario quiso operar (para mostrarlo en el modal). */
  monto: number;
  /** Moneda — solo para formatear (VES → Bs, USD → $). */
  moneda: 'VES' | 'USD';
  /** Umbral vigente, para mostrar al usuario por qué se le pide. */
  umbral: number | null;
  /** Si la operación está en curso (deshabilita botones). */
  procesando?: boolean;
  /** Cierre sin confirmar. */
  onCancelar: () => void;
  /** Confirmación: el padre debe relanzar la operación con estos datos. */
  onConfirmar: (datos: { confirmaOrigenLicito: true; origenFondos: string }) => void;
}

const formatMonto = (monto: number, moneda: 'VES' | 'USD') => {
  const simbolo = moneda === 'VES' ? 'Bs' : '$';
  return `${simbolo} ${monto.toLocaleString('es-VE', { minimumFractionDigits: 2 })}`;
};

export function LocdoftOperacionModal({
  open,
  tipoOperacion,
  monto,
  moneda,
  umbral,
  procesando = false,
  onCancelar,
  onConfirmar,
}: LocdoftOperacionModalProps) {
  const [aceptaOrigenLicito, setAceptaOrigenLicito] = useState(false);
  const [origenFondos, setOrigenFondos] = useState('');

  // Reset cuando se abre/cierra el modal
  useEffect(() => {
    if (open) {
      setAceptaOrigenLicito(false);
      setOrigenFondos('');
    }
  }, [open]);

  const verbo = tipoOperacion === 'deposito' ? 'depositar' : 'retirar';

  const handleConfirmar = () => {
    if (!aceptaOrigenLicito || procesando) return;
    onConfirmar({ confirmaOrigenLicito: true, origenFondos: origenFondos.trim() });
  };

  return (
    <Dialog open={open} onOpenChange={(o) => { if (!o && !procesando) onCancelar(); }}>
      <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-amber-900">
            <AlertTriangle className="w-5 h-5 text-amber-600" aria-hidden="true" />
            Declaración jurada requerida (LOCDOFT)
          </DialogTitle>
          <DialogDescription>
            Esta operación supera el umbral de monitoreo establecido por la
            Ley Orgánica contra la Delincuencia Organizada y Financiamiento al
            Terrorismo (LOCDOFT). Necesitamos tu declaración jurada antes de
            continuar.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          {/* Resumen de la operación */}
          <div className="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm">
            <p className="text-amber-900">
              Estás por <strong>{verbo}</strong>{' '}
              <strong className="text-base">{formatMonto(monto, moneda)}</strong>
              {umbral !== null && (
                <>
                  {' '}— umbral vigente:{' '}
                  <span className="font-mono">{formatMonto(umbral, moneda)}</span>
                </>
              )}
              .
            </p>
          </div>

          {/* Checkbox obligatorio */}
          <label className="flex items-start gap-3 rounded-lg border border-slate-200 p-3 hover:bg-slate-50 cursor-pointer">
            <Checkbox
              id="acepta-origen-licito"
              checked={aceptaOrigenLicito}
              onCheckedChange={(c) => setAceptaOrigenLicito(c === true)}
              disabled={procesando}
              className="mt-1"
              aria-label="Declaro que los fondos provienen de actividades lícitas"
            />
            <Label htmlFor="acepta-origen-licito" className="text-sm leading-relaxed text-slate-800 cursor-pointer">
              <strong className="block mb-1 text-[#0F2744]">Declaración jurada</strong>
              Declaro bajo fe de juramento que los fondos involucrados en esta
              operación <strong>no provienen de actividades ilícitas</strong>,
              de acuerdo con la Ley Orgánica contra la Delincuencia Organizada
              y Financiamiento al Terrorismo (LOCDOFT).
            </Label>
          </label>

          {/* Campo opcional */}
          <div className="space-y-2">
            <Label htmlFor="origen-fondos" className="text-sm font-medium text-slate-700">
              Origen de los fondos <span className="text-xs font-normal text-slate-500">(opcional)</span>
            </Label>
            <Textarea
              id="origen-fondos"
              value={origenFondos}
              onChange={(e) => setOrigenFondos(e.target.value)}
              disabled={procesando}
              maxLength={2000}
              placeholder="Ej: ahorros personales, venta de vehículo, herencia..."
              className="min-h-[80px] resize-y"
              aria-describedby="origen-fondos-help"
            />
            <p id="origen-fondos-help" className="text-xs text-slate-500">
              Información que ayuda al departamento de cumplimiento. Útil si
              estás registrado como PEP (Persona Expuesta Políticamente) o
              quieres justificar voluntariamente el origen.
            </p>
          </div>

          {/* Acciones */}
          <div className="flex flex-col sm:flex-row gap-2 sm:justify-end pt-2 border-t border-slate-100">
            <Button
              type="button"
              variant="outline"
              onClick={onCancelar}
              disabled={procesando}
            >
              Cancelar
            </Button>
            <Button
              type="button"
              onClick={handleConfirmar}
              disabled={!aceptaOrigenLicito || procesando}
              className="bg-[#16A34A] hover:bg-green-700 gap-2"
            >
              <ShieldCheck className="w-4 h-4" aria-hidden="true" />
              {procesando ? 'Procesando...' : 'Confirmar y continuar'}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
