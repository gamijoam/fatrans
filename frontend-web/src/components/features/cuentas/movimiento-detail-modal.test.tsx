import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import {
  MovimientoDetailModal,
  type MovimientoDetail,
} from '@/components/features/cuentas/movimiento-detail-modal';

/**
 * Tests del modal de detalle de movimiento (issue #220).
 * Cubre: render condicional, secciones del modal, cálculo del delta,
 * etiqueta de canal, botón de comprobante deshabilitado (placeholder
 * hasta PR-B con endpoint backend).
 */

const movimientoDeposito: MovimientoDetail = {
  id: 'mov-1',
  numeroOperacion: 'OP-2026-000001',
  tipo: 'DEPOSITO',
  monto: 1500.5,
  saldoAnterior: 8000,
  saldoPosterior: 9500.5,
  descripcion: 'Depósito en caja Centro',
  referencia: 'REF-XYZ-123',
  canalOrigen: 'CAJA',
  estado: 'COMPLETADO',
  fechaMovimiento: '2026-05-17T14:30:45',
  fechaValor: '2026-05-17T14:30:45',
};

const movimientoRetiro: MovimientoDetail = {
  id: 'mov-2',
  numeroOperacion: 'OP-2026-000002',
  tipo: 'RETIRO',
  monto: 250,
  saldoAnterior: 9500.5,
  saldoPosterior: 9250.5,
  descripcion: null,
  referencia: null,
  canalOrigen: 'WEB',
  estado: 'PENDIENTE',
  fechaMovimiento: '2026-05-17T15:00:00',
  fechaValor: '2026-05-17T15:00:00',
};

describe('MovimientoDetailModal', () => {
  it('no renderiza cuando movimiento es null', () => {
    render(<MovimientoDetailModal movimiento={null} moneda="VES" onClose={vi.fn()} />);
    expect(screen.queryByText(/Detalle del movimiento/i)).toBeNull();
  });

  it('renderiza secciones principales cuando hay movimiento (depósito)', () => {
    render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="VES" onClose={vi.fn()} />);

    expect(screen.getByText(/Detalle del movimiento/i)).toBeTruthy();
    // Header con el numero de operación
    expect(screen.getAllByText('OP-2026-000001').length).toBeGreaterThan(0);
    // 4 secciones
    expect(screen.getByText(/Datos básicos/i)).toBeTruthy();
    expect(screen.getByText(/Referencias/i)).toBeTruthy();
    expect(screen.getByText(/Saldos/i)).toBeTruthy();
    expect(screen.getByText(/Canal/i)).toBeTruthy();
  });

  it('muestra signo + y monto formateado para depósitos', () => {
    render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="VES" onClose={vi.fn()} />);
    // El monto se muestra con signo + y prefijo Bs (VES)
    expect(screen.getByText(/\+ Bs/)).toBeTruthy();
  });

  it('muestra signo − y monto formateado para retiros', () => {
    render(<MovimientoDetailModal movimiento={movimientoRetiro} moneda="VES" onClose={vi.fn()} />);
    expect(screen.getByText(/− Bs/)).toBeTruthy();
  });

  it('mapea canal CAJA a etiqueta legible "Caja presencial"', () => {
    render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="VES" onClose={vi.fn()} />);
    expect(screen.getByText(/Caja presencial/i)).toBeTruthy();
  });

  it('mapea canal WEB a etiqueta "Banca en línea"', () => {
    render(<MovimientoDetailModal movimiento={movimientoRetiro} moneda="VES" onClose={vi.fn()} />);
    expect(screen.getByText(/Banca en línea/i)).toBeTruthy();
  });

  it('estado PENDIENTE se renderiza con su etiqueta legible', () => {
    render(<MovimientoDetailModal movimiento={movimientoRetiro} moneda="VES" onClose={vi.fn()} />);
    expect(screen.getByText('Pendiente')).toBeTruthy();
  });

  it('omite la sección de referencia externa cuando es null', () => {
    render(<MovimientoDetailModal movimiento={movimientoRetiro} moneda="VES" onClose={vi.fn()} />);
    expect(screen.queryByText(/Referencia externa/i)).toBeNull();
  });

  it('botón "Descargar comprobante" está deshabilitado (placeholder PR-B)', () => {
    render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="VES" onClose={vi.fn()} />);
    const btn = screen.getByRole('button', { name: /descargar comprobante/i });
    expect(btn.hasAttribute('disabled')).toBe(true);
  });

  it('botón Cerrar invoca onClose', () => {
    const onClose = vi.fn();
    render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="VES" onClose={onClose} />);
    fireEvent.click(screen.getByRole('button', { name: /cerrar/i }));
    expect(onClose).toHaveBeenCalled();
  });

  it('formatea moneda USD con prefijo $ en lugar de Bs', () => {
    render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="USD" onClose={vi.fn()} />);
    expect(screen.getByText(/\+ \$/)).toBeTruthy();
  });
});
