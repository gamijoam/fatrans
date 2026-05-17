import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import {
  MovimientoDetailModal,
  type MovimientoDetail,
} from '@/components/features/cuentas/movimiento-detail-modal';

// Sonner — mock para no contar con DOM extra en los tests
vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

/**
 * Tests del modal de detalle de movimiento (issue #220 PR-A + PR-B).
 * Cubre: render condicional, secciones del modal, etiquetas legibles,
 * descarga del comprobante PDF (botón habilitado, fetch, toast).
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
    render(<MovimientoDetailModal movimiento={null} moneda="VES" numeroCuenta="AHO-2026-000001" onClose={vi.fn()} />);
    expect(screen.queryByText(/Detalle del movimiento/i)).toBeNull();
  });

  it('renderiza secciones principales cuando hay movimiento (depósito)', () => {
    render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="VES" numeroCuenta="AHO-2026-000001" onClose={vi.fn()} />);

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
    render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="VES" numeroCuenta="AHO-2026-000001" onClose={vi.fn()} />);
    // El monto se muestra con signo + y prefijo Bs (VES)
    expect(screen.getByText(/\+ Bs/)).toBeTruthy();
  });

  it('muestra signo − y monto formateado para retiros', () => {
    render(<MovimientoDetailModal movimiento={movimientoRetiro} moneda="VES" numeroCuenta="AHO-2026-000001" onClose={vi.fn()} />);
    expect(screen.getByText(/− Bs/)).toBeTruthy();
  });

  it('mapea canal CAJA a etiqueta legible "Caja presencial"', () => {
    render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="VES" numeroCuenta="AHO-2026-000001" onClose={vi.fn()} />);
    expect(screen.getByText(/Caja presencial/i)).toBeTruthy();
  });

  it('mapea canal WEB a etiqueta "Banca en línea"', () => {
    render(<MovimientoDetailModal movimiento={movimientoRetiro} moneda="VES" numeroCuenta="AHO-2026-000001" onClose={vi.fn()} />);
    expect(screen.getByText(/Banca en línea/i)).toBeTruthy();
  });

  it('estado PENDIENTE se renderiza con su etiqueta legible', () => {
    render(<MovimientoDetailModal movimiento={movimientoRetiro} moneda="VES" numeroCuenta="AHO-2026-000001" onClose={vi.fn()} />);
    expect(screen.getByText('Pendiente')).toBeTruthy();
  });

  it('omite la sección de referencia externa cuando es null', () => {
    render(<MovimientoDetailModal movimiento={movimientoRetiro} moneda="VES" numeroCuenta="AHO-2026-000001" onClose={vi.fn()} />);
    expect(screen.queryByText(/Referencia externa/i)).toBeNull();
  });

  it('botón "Descargar comprobante" está habilitado con aria-label descriptivo', () => {
    render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="VES" numeroCuenta="AHO-2026-000001" onClose={vi.fn()} />);
    const btn = screen.getByRole('button', { name: /descargar comprobante pdf del movimiento op-2026-000001/i });
    expect(btn.hasAttribute('disabled')).toBe(false);
  });

  it('botón Cerrar invoca onClose', () => {
    const onClose = vi.fn();
    render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="VES" numeroCuenta="AHO-2026-000001" onClose={onClose} />);
    fireEvent.click(screen.getByRole('button', { name: /cerrar/i }));
    expect(onClose).toHaveBeenCalled();
  });

  it('formatea moneda USD con prefijo $ en lugar de Bs', () => {
    render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="USD" numeroCuenta="AHO-2026-000001" onClose={vi.fn()} />);
    expect(screen.getByText(/\+ \$/)).toBeTruthy();
  });

  describe('descarga de comprobante (PR-B)', () => {
    beforeEach(() => {
      vi.clearAllMocks();
      // jsdom no implementa URL.createObjectURL / revokeObjectURL
      Object.defineProperty(URL, 'createObjectURL', {
        configurable: true,
        value: vi.fn(() => 'blob:mock-url'),
      });
      Object.defineProperty(URL, 'revokeObjectURL', {
        configurable: true,
        value: vi.fn(),
      });
    });

    afterEach(() => {
      vi.restoreAllMocks();
    });

    it('click llama al BFF con la URL correcta', async () => {
      const mockBlob = new Blob([new Uint8Array([0x25, 0x50, 0x44, 0x46])], { type: 'application/pdf' });
      const fetchMock = vi.fn().mockResolvedValue({
        ok: true,
        blob: () => Promise.resolve(mockBlob),
      });
      global.fetch = fetchMock as unknown as typeof fetch;

      render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="VES" numeroCuenta="AHO-2026-000001" onClose={vi.fn()} />);
      fireEvent.click(screen.getByRole('button', { name: /descargar comprobante pdf/i }));

      await waitFor(() => {
        expect(fetchMock).toHaveBeenCalledWith(
          '/api/cuentas/AHO-2026-000001/movimientos/OP-2026-000001/comprobante',
        );
      });
    });

    it('si BFF devuelve error, muestra toast.error con el mensaje del backend', async () => {
      const { toast } = await import('sonner');
      const fetchMock = vi.fn().mockResolvedValue({
        ok: false,
        status: 403,
        json: () => Promise.resolve({ message: 'Acceso denegado' }),
      });
      global.fetch = fetchMock as unknown as typeof fetch;

      render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="VES" numeroCuenta="AHO-2026-000001" onClose={vi.fn()} />);
      fireEvent.click(screen.getByRole('button', { name: /descargar comprobante pdf/i }));

      await waitFor(() => {
        expect(toast.error).toHaveBeenCalledWith('Acceso denegado');
      });
    });

    it('si fetch lanza (red caída), muestra toast.error genérico', async () => {
      const { toast } = await import('sonner');
      const fetchMock = vi.fn().mockRejectedValue(new Error('NetworkError'));
      global.fetch = fetchMock as unknown as typeof fetch;

      render(<MovimientoDetailModal movimiento={movimientoDeposito} moneda="VES" numeroCuenta="AHO-2026-000001" onClose={vi.fn()} />);
      fireEvent.click(screen.getByRole('button', { name: /descargar comprobante pdf/i }));

      await waitFor(() => {
        expect(toast.error).toHaveBeenCalledWith('Error de red al descargar el comprobante');
      });
    });
  });
});
