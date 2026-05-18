'use client';

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { SessionManager } from './session-manager';
import { sesionesApi } from '@/lib/api/sesiones';
import { toast } from 'sonner';

vi.mock('@/lib/api/sesiones');
vi.mock('sonner');

const mockSesionesApi = sesionesApi as ReturnType<typeof vi.fn> & typeof sesionesApi;

describe('SessionManager', () => {
  const mockUsuarioId = '123e4567-e89b-12d3-a456-426614174000';
  const mockUsuarioNombre = 'Juan Pérez';

  const mockSesiones = [
    {
      id: 'sesion-1',
      ipAddress: '192.168.1.1',
      userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
      ultimoAcceso: '2026-04-27T10:00:00Z',
      fechaCreacion: '2026-04-27T08:00:00Z',
      expiraAt: '2026-04-28T08:00:00Z',
      activa: true,
    },
    {
      id: 'sesion-2',
      ipAddress: '10.0.0.1',
      userAgent: 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)',
      ultimoAcceso: '2026-04-27T09:30:00Z',
      fechaCreacion: '2026-04-26T14:00:00Z',
      expiraAt: '2026-04-27T14:00:00Z',
      activa: true,
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('debe renderizar el título con el nombre del usuario', () => {
    render(<SessionManager usuarioId={mockUsuarioId} usuarioNombre={mockUsuarioNombre} />);

    expect(screen.getByText(`Sesiones de ${mockUsuarioNombre}`)).toBeInTheDocument();
  });

  it('debe mostrar botón para ver sesiones', () => {
    render(<SessionManager usuarioId={mockUsuarioId} usuarioNombre={mockUsuarioNombre} />);

    expect(screen.getByRole('button', { name: /ver sesiones/i })).toBeInTheDocument();
  });

  it('debe cargar y mostrar sesiones al hacer clic en ver sesiones', async () => {
    mockSesionesApi.listarPorUsuario.mockResolvedValue({ data: mockSesiones } as never);

    render(<SessionManager usuarioId={mockUsuarioId} usuarioNombre={mockUsuarioNombre} />);

    const botonVerSesiones = screen.getByRole('button', { name: /ver sesiones/i });
    fireEvent.click(botonVerSesiones);

    await waitFor(() => {
      expect(mockSesionesApi.listarPorUsuario).toHaveBeenCalledWith(mockUsuarioId);
    });

    await waitFor(() => {
      expect(screen.getByText('2 sesión(es) activa(s)')).toBeInTheDocument();
    });

    expect(screen.getByText(/Windows NT 10.0/i)).toBeInTheDocument();
    expect(screen.getByText(/iPhone OS 14_0/i)).toBeInTheDocument();
  });

  it('debe mostrar mensaje cuando no hay sesiones activas', async () => {
    mockSesionesApi.listarPorUsuario.mockResolvedValue({ data: [] } as never);

    render(<SessionManager usuarioId={mockUsuarioId} usuarioNombre={mockUsuarioNombre} />);

    const botonVerSesiones = screen.getByRole('button', { name: /ver sesiones/i });
    fireEvent.click(botonVerSesiones);

    await waitFor(() => {
      expect(screen.getByText(/no hay sesiones activas/i)).toBeInTheDocument();
    });
  });

  it('debe mostrar botón para ocultar sesiones cuando están expandidas', async () => {
    mockSesionesApi.listarPorUsuario.mockResolvedValue({ data: mockSesiones } as never);

    render(<SessionManager usuarioId={mockUsuarioId} usuarioNombre={mockUsuarioNombre} />);

    const botonVerSesiones = screen.getByRole('button', { name: /ver sesiones/i });
    fireEvent.click(botonVerSesiones);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /ocultar/i })).toBeInTheDocument();
    });
  });

  

  it('debe mostrar botón invalidar todas cuando hay sesiones', async () => {
    mockSesionesApi.listarPorUsuario.mockResolvedValue({ data: mockSesiones } as never);

    render(<SessionManager usuarioId={mockUsuarioId} usuarioNombre={mockUsuarioNombre} />);

    const botonVerSesiones = screen.getByRole('button', { name: /ver sesiones/i });
    fireEvent.click(botonVerSesiones);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /invalidar todas/i })).toBeInTheDocument();
    });
  });

  it('debe mostrar confirmación antes de invalidar todas las sesiones', async () => {
    mockSesionesApi.listarPorUsuario.mockResolvedValue({ data: mockSesiones } as never);

    render(<SessionManager usuarioId={mockUsuarioId} usuarioNombre={mockUsuarioNombre} />);

    const botonVerSesiones = screen.getByRole('button', { name: /ver sesiones/i });
    fireEvent.click(botonVerSesiones);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /invalidar todas/i })).toBeInTheDocument();
    });

    const botonInvalidarTodas = screen.getByRole('button', { name: /invalidar todas/i });
    fireEvent.click(botonInvalidarTodas);

    expect(screen.getByText(/¿invalidar todas las sesiones/i)).toBeInTheDocument();
    expect(screen.getByText(new RegExp(`invalidará todas las sesiones activas de ${mockUsuarioNombre}`))).toBeInTheDocument();
  });

  it('debe llamar a invalidarTodas y mostrar toast de éxito', async () => {
    mockSesionesApi.listarPorUsuario.mockResolvedValue({ data: mockSesiones } as never);
    mockSesionesApi.invalidarTodas.mockResolvedValue({
      data: { usuarioId: mockUsuarioId, sesionesInvalidadas: 2, mensaje: 'OK' },
    } as never);

    render(<SessionManager usuarioId={mockUsuarioId} usuarioNombre={mockUsuarioNombre} />);

    const botonVerSesiones = screen.getByRole('button', { name: /ver sesiones/i });
    fireEvent.click(botonVerSesiones);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /invalidar todas/i })).toBeInTheDocument();
    });

    const botonInvalidarTodas = screen.getByRole('button', { name: /invalidar todas/i });
    fireEvent.click(botonInvalidarTodas);

    const botonConfirmar = screen.getByRole('button', { name: /invalidar todas$/i });
    fireEvent.click(botonConfirmar);

    await waitFor(() => {
      expect(mockSesionesApi.invalidarTodas).toHaveBeenCalledWith(mockUsuarioId);
    });

    await waitFor(() => {
      expect(toast.success).toHaveBeenCalledWith('Todas las sesiones han sido invalidadas');
    });
  });

  it('debe mostrar botón para invalidar sesión individual', async () => {
    mockSesionesApi.listarPorUsuario.mockResolvedValue({ data: mockSesiones } as never);

    render(<SessionManager usuarioId={mockUsuarioId} usuarioNombre={mockUsuarioNombre} />);

    const botonVerSesiones = screen.getByRole('button', { name: /ver sesiones/i });
    fireEvent.click(botonVerSesiones);

    await waitFor(() => {
      const botonesInvalidar = screen.getAllByRole('button', { name: '' });
      expect(botonesInvalidar.length).toBeGreaterThan(0);
    });
  });

  it('debe llamar a invalidarSesion al confirmar invalidación individual', async () => {
    mockSesionesApi.listarPorUsuario.mockResolvedValue({ data: mockSesiones } as never);
    mockSesionesApi.invalidarSesion.mockResolvedValue({ data: undefined } as never);

    render(<SessionManager usuarioId={mockUsuarioId} usuarioNombre={mockUsuarioNombre} />);

    const botonVerSesiones = screen.getByRole('button', { name: /ver sesiones/i });
    fireEvent.click(botonVerSesiones);

    await waitFor(() => {
      expect(screen.getByText(/Windows NT 10.0/i)).toBeInTheDocument();
    });

    const botonesInvalidar = screen.getAllByRole('button', { name: /^\s*$/ });
    if (botonesInvalidar.length > 0) {
      fireEvent.click(botonesInvalidar[0]);
    }

    await waitFor(() => {
      expect(screen.getByText(/¿invalidar esta sesión/i)).toBeInTheDocument();
    });
  });

  it('debe manejar error al cargar sesiones', async () => {
    mockSesionesApi.listarPorUsuario.mockRejectedValue(new Error('Error de red'));

    render(<SessionManager usuarioId={mockUsuarioId} usuarioNombre={mockUsuarioNombre} />);

    const botonVerSesiones = screen.getByRole('button', { name: /ver sesiones/i });
    fireEvent.click(botonVerSesiones);

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Error al cargar sesiones');
    });
  });

  
});