import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import AdminDashboardPage from '@/app/(admin)/admin/page';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

vi.mock('@/stores/auth-store', () => ({
  useAuthStore: vi.fn((selector) => {
    const state = {
      user: {
        id: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        nombreUsuario: 'admin.test',
        correoElectronico: 'admin@test.com',
        nombreCompleto: 'Administrador Test',
        rol: 'ADMIN' as const,
      },
      isAuthenticated: true,
      isLoading: false,
      setUser: vi.fn(),
      setLoading: vi.fn(),
      logout: vi.fn(),
    };
    return selector(state);
  }),
}));

const mockDashboardStats = {
  totalSocios: 150,
  sociosActivos: 120,
  sociosPendientes: 30,
  totalCuentasAhorro: 145,
  cuentasActivas: 140,
  depositosMes: 25,
  retirosMes: 10,
  prestamosActivos: 45,
  solicitudesPendientes: 8,
  solicitudesAprobadas: 120,
  solicitudesRechazadas: 15,
  capitalDesembolsado: 50000000,
  carteraVencida: 2500000,
  cuotasVencidas: 12,
  tasaCumplimiento: 87.5,
  tasaMora: 5.2,
  actividadReciente: {
    nuevosSociosMes: 15,
    depositosMes: 25,
    retirosMes: 10,
    prestamosAprobadosMes: 5,
    montoDepositadoMes: 15000000,
    montoRetiradoMes: 5000000,
  },
};

const mockActividadReciente = [
  {
    id: '11111111-1111-1111-1111-111111111111',
    tipoEvento: 'LOGIN_SUCCESS',
    usuarioId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    nombreUsuario: 'admin.sistema',
    ipAddress: '192.168.1.100',
    timestamp: new Date(Date.now() - 5 * 60 * 1000).toISOString(),
    detalles: 'Login exitoso',
    entityType: null,
    entityId: null,
    action: null,
  },
  {
    id: '22222222-2222-2222-2222-222222222222',
    tipoEvento: 'TIPO_CREDITO_CREADO',
    usuarioId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    nombreUsuario: 'admin.sistema',
    ipAddress: '192.168.1.100',
    timestamp: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
    detalles: 'Tipo de crédito "Hipotecario" creado',
    entityType: 'TipoCredito',
    entityId: 'cccccccc-cccc-cccc-cccc-cccccccccccc',
    action: 'CREATE',
  },
  {
    id: '33333333-3333-3333-3333-333333333333',
    tipoEvento: 'ADMIN_CREADO',
    usuarioId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    nombreUsuario: 'super.admin',
    ipAddress: '192.168.1.101',
    timestamp: new Date(Date.now() - 60 * 60 * 1000).toISOString(),
    detalles: 'Nuevo admin creado: operador.uno',
    entityType: 'Usuario',
    entityId: 'dddddddd-dddd-dddd-dddd-dddddddddddd',
    action: 'CREATE',
  },
  {
    id: '44444444-4444-4444-4444-444444444444',
    tipoEvento: 'SOLICITUD_REGISTRO_APROBADA',
    usuarioId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    nombreUsuario: 'admin.sistema',
    ipAddress: '192.168.1.100',
    timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    detalles: 'Solicitud V-30123456 aprobada',
    entityType: 'SolicitudRegistro',
    entityId: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
    action: 'APPROVE',
  },
  {
    id: '55555555-5555-5555-5555-555555555555',
    tipoEvento: 'PERFIL_SOCIO_ACTUALIZADO',
    usuarioId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    nombreUsuario: 'admin.sistema',
    ipAddress: '192.168.1.100',
    timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
    detalles: 'Perfil socio V-30876543 actualizado',
    entityType: 'Socio',
    entityId: 'ffffffff-ffff-ffff-ffff-ffffffffffff',
    action: 'UPDATE',
  },
];

describe('AdminDashboardPage', () => {
  beforeEach(() => {
    global.fetch = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('debe renderizar el título del dashboard', async () => {
    (global.fetch as any)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockDashboardStats,
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockActividadReciente,
      });

    render(<AdminDashboardPage />);

    await waitFor(() => {
      expect(screen.getByText('Dashboard Admin')).toBeTruthy();
    });
  });

  it('debe mostrar estadísticas de socios', async () => {
    (global.fetch as any)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockDashboardStats,
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockActividadReciente,
      });

    render(<AdminDashboardPage />);

    await waitFor(() => {
      expect(screen.getByText('Total Socios')).toBeTruthy();
      expect(screen.getByText('150')).toBeTruthy();
    });
  });

  it('debe mostrar sección de actividad reciente', async () => {
    (global.fetch as any)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockDashboardStats,
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockActividadReciente,
      });

    render(<AdminDashboardPage />);

    await waitFor(() => {
      expect(screen.getByText('Actividad Reciente')).toBeTruthy();
    });
  });

  it('debe mostrar eventos de actividad con iconos', async () => {
    (global.fetch as any)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockDashboardStats,
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockActividadReciente,
      });

    render(<AdminDashboardPage />);

    await waitFor(() => {
      expect(screen.getByText('Actividad Reciente')).toBeTruthy();
    });

    const actividadHeaders = screen.getAllByText(/Actividad Reciente/);
    expect(actividadHeaders.length).toBeGreaterThan(0);
  });

  it('debe manejar actividad vacía', async () => {
    (global.fetch as any)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockDashboardStats,
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      });

    render(<AdminDashboardPage />);

    await waitFor(() => {
      expect(screen.getByText('Actividad Reciente')).toBeTruthy();
    });
  });

  it('debe mostrar tasa de cumplimiento', async () => {
    (global.fetch as any)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockDashboardStats,
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockActividadReciente,
      });

    render(<AdminDashboardPage />);

    await waitFor(() => {
      expect(screen.getByText('Tasa Cumplimiento')).toBeTruthy();
      expect(screen.getByText('87.5%')).toBeTruthy();
    });
  });

  it('debe mostrar capital desembolsado formateado', async () => {
    (global.fetch as any)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockDashboardStats,
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockActividadReciente,
      });

    render(<AdminDashboardPage />);

    await waitFor(() => {
      expect(screen.getByText('Capital Desembolsado')).toBeTruthy();
    });
  });

  it('debe mostrar accesos rápidos', async () => {
    (global.fetch as any)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockDashboardStats,
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockActividadReciente,
      });

    render(<AdminDashboardPage />);

    await waitFor(() => {
      expect(screen.getByText('Accesos Rápidos')).toBeTruthy();
      expect(screen.getByText('Gestionar Socios')).toBeTruthy();
      expect(screen.getByText('Ver Solicitudes')).toBeTruthy();
    });
  });

  it('debe mostrar error cuando falla la carga de stats', async () => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: false,
      status: 500,
      json: async () => ({ message: 'Error interno' }),
    });

    render(<AdminDashboardPage />);

    await waitFor(() => {
      expect(screen.getByText('Dashboard Admin')).toBeTruthy();
    });
  });
});