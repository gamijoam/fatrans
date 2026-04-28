import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent, act } from '@testing-library/react';
import AdminParametrosPage from '@/app/(admin)/admin/configuracion/page';

const mockParametros = [
  {
    key: 'TASA_INTERES_AHORRO',
    valor: '0.05',
    tipo: 'PERCENTAGE',
    descripcion: 'Tasa de interés anual para cuentas de ahorro',
    categoria: 'TASA',
    editable: true,
    fechaActualizacion: '2026-04-26T10:00:00Z',
    actualizadoPor: 'admin-1',
  },
  {
    key: 'LIMITE_RETIRO_DIARIO',
    valor: '50000',
    tipo: 'NUMERIC',
    descripcion: 'Límite máximo de retiro diario por cuenta',
    categoria: 'LIMITE',
    editable: true,
    fechaActualizacion: '2026-04-25T15:30:00Z',
    actualizadoPor: 'admin-1',
  },
  {
    key: 'MONEDA_PRINCIPAL',
    valor: 'VES',
    tipo: 'STRING',
    descripcion: 'Moneda principal del sistema',
    categoria: 'SISTEMA',
    editable: false,
    fechaActualizacion: null,
    actualizadoPor: null,
  },
];

describe('AdminParametrosPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('debe renderizar el título de parámetros del sistema', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockParametros,
    } as any);

    render(<AdminParametrosPage />);

    expect(screen.getByText('Parámetros del Sistema')).toBeTruthy();
  });

  it('debe cargar parámetros al montar', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockParametros,
    } as any);

    render(<AdminParametrosPage />);

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/admin/parametros',
        expect.objectContaining({ credentials: 'include' })
      );
    });
  });

  it('debe mostrar parámetros agrupados por categoría', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockParametros,
    } as any);

    render(<AdminParametrosPage />);

    await waitFor(() => {
      expect(screen.getByText('TASA')).toBeTruthy();
      expect(screen.getByText('LIMITE')).toBeTruthy();
      expect(screen.getByText('SISTEMA')).toBeTruthy();
    });
  });

  it('debe mostrar clave y valor del parámetro', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockParametros,
    } as any);

    render(<AdminParametrosPage />);

    await waitFor(() => {
      expect(screen.getByText('TASA_INTERES_AHORRO')).toBeTruthy();
    });
  });

  it('debe mostrar icono de candado para parámetros no editables', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockParametros,
    } as any);

    render(<AdminParametrosPage />);

    await waitFor(() => {
      const lockIcon = document.querySelector('[class*="lucide-lock"]');
      expect(lockIcon).toBeTruthy();
    });
  });

  it('debe mostrar descripción del parámetro', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockParametros,
    } as any);

    render(<AdminParametrosPage />);

    await waitFor(() => {
      expect(screen.getByText(/Tasa de interés anual/)).toBeTruthy();
    });
  });

  it('debe mostrar botón editar para parámetros editables', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockParametros,
    } as any);

    render(<AdminParametrosPage />);

    await waitFor(() => {
      const editButtons = screen.getAllByRole('button', { name: /editar/i });
      expect(editButtons.length).toBe(2);
    });
  });

  it('debe abrir formulario de edición al hacer clic en editar', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockParametros,
    } as any);

    render(<AdminParametrosPage />);

    await waitFor(() => {
      expect(screen.getByText('TASA')).toBeTruthy();
    });

    const editButtons = screen.getAllByRole('button', { name: /editar/i });
    fireEvent.click(editButtons[0]);

    await waitFor(() => {
      expect(screen.getByText('Guardar')).toBeTruthy();
    });
  });

  it('debe manejar errores al cargar parámetros', async () => {
    global.fetch.mockResolvedValue({
      ok: false,
      status: 500,
    } as any);

    render(<AdminParametrosPage />);

    expect(screen.getByText('Parámetros del Sistema')).toBeTruthy();
  });

  it('debe manejar respuesta vacía', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => [],
    } as any);

    render(<AdminParametrosPage />);

    await waitFor(() => {
      expect(screen.getByText('No hay parámetros configurados')).toBeTruthy();
    });
  });

  it('debe llamar a la API con filtro de categoría', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => [mockParametros[0]],
    } as any);

    render(<AdminParametrosPage />);

    await waitFor(() => {});

    const select = screen.getByRole('combobox');
    fireEvent.change(select, { target: { value: 'TASA' } });

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/admin/parametros?categoria=TASA',
        expect.objectContaining({ credentials: 'include' })
      );
    });
  });

  it('debe llamar a la API para actualizar parámetro', async () => {
    global.fetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockParametros,
      } as any)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ ...mockParametros[0], valor: '0.06' }),
      } as any);

    render(<AdminParametrosPage />);

    await waitFor(() => {
      expect(screen.getByText('TASA')).toBeTruthy();
    });

    const editButtons = screen.getAllByRole('button', { name: /editar/i });
    fireEvent.click(editButtons[0]);

    await act(async () => {
      const input = screen.getByDisplayValue('0.05');
      fireEvent.change(input, { target: { value: '0.06' } });
    });

    const saveButtons = screen.getAllByRole('button', { name: /guardar/i });
    fireEvent.click(saveButtons[0]);

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/admin/parametros',
        expect.objectContaining({
          method: 'PUT',
        })
      );
    });
  });
});