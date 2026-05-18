import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent, act } from '@testing-library/react';
import { useParams } from 'next/navigation';
import SocioDetallePage from '@/app/(admin)/admin/socios/[id]/page';

vi.mock('next/navigation', () => ({
  useParams: vi.fn(),
  useRouter: vi.fn(() => ({
    push: vi.fn(),
  })),
}));

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

const mockSocio = {
  id: '11111111-1111-1111-1111-111111111111',
  numeroSocio: 'SOC-2024-001',
  tipoDocumento: 'CEDULA',
  numeroDocumento: 'V-30123456',
  primerNombre: 'Juan',
  segundoNombre: 'Carlos',
  primerApellido: 'Pérez',
  segundoApellido: 'García',
  correoElectronico: 'juan.perez@test.com',
  telefonoPrincipal: '04121234567',
  telefonoSecundario: '',
  empresa: 'Empresa ABC',
  departamento: 'Recursos Humanos',
  cargo: 'Analista',
  tipoContrato: 'TIEMPO_COMPLETO',
  salario: 5000000,
  montoAhorro: 1500000,
  estado: 'ACTIVO',
  fechaIngreso: '2024-01-15T10:30:00Z',
  fechaRegistro: '2024-01-15T10:30:00Z',
  fechaActivacion: '2024-01-16T10:30:00Z',
  fechaDesactivacion: null,
  motivoDesactivacion: null,
};

const mockCuentas = [
  {
    id: 'cuenta-1',
    numeroCuenta: '1234-5678-9012-3456',
    tipoCuenta: 'AHORRO_MENSUAL',
    saldo: 1500000,
    estado: 'ACTIVA',
  },
];

const mockBeneficiarios = [
  {
    id: 'ben-1',
    nombreCompleto: 'María Pérez',
    tipoDocumento: 'CEDULA',
    numeroDocumento: 'V-30234567',
    parentesco: 'CONYUGE',
    porcentaje: 100,
    telefono: '04141234567',
    activo: true,
  },
];

const mockCreditos = [
  {
    id: 'cred-1',
    numeroSolicitud: 'CRED-2024-001',
    tipoCreditoNombre: 'Micro Crédito',
    montoSolicitado: 5000000,
    plazoMeses: 12,
    estado: 'APROBADA',
    createdAt: '2024-02-01T10:30:00Z',
  },
];

const mockKycAprobado = {
  verificacionId: 'kyc-123',
  socioId: '11111111-1111-1111-1111-111111111111',
  nivel: 'BASICO',
  estado: 'APROBADO',
  fechaInicio: '2024-01-15T10:30:00Z',
  fechaCompletado: '2024-01-16T10:30:00Z',
  fechaExpiracion: '2025-01-16T10:30:00Z',
  revisadoPor: 'admin@test.com',
  motivoRechazo: '',
};

const mockKycSinKyc = {
  socioId: '11111111-1111-1111-1111-111111111111',
  estado: 'SIN_KYC',
  mensaje: 'El socio no tiene verificaciones KYC',
};

describe('SocioDetallePage', () => {
  beforeEach(() => {
    (useParams as any).mockReturnValue({ id: '11111111-1111-1111-1111-111111111111' });
    global.fetch = vi.fn();
  });

  it('debe renderizar información del socio', async () => {
    (global.fetch as any).mockImplementation((url: string) => {
      if (url.includes('/cuentas')) return { ok: true, json: async () => mockCuentas };
      if (url.includes('/beneficiarios')) return { ok: true, json: async () => mockBeneficiarios };
      if (url.includes('/creditos')) return { ok: true, json: async () => mockCreditos };
      if (url.includes('/kyc')) return { ok: true, json: async () => mockKycAprobado };
      return { ok: true, json: async () => mockSocio };
    });

    render(<SocioDetallePage />);

    await waitFor(() => {
      expect(screen.getByText('Juan Carlos Pérez García')).toBeTruthy();
    });
  });

  it('debe mostrar tabs de Cuentas, Beneficiarios, Créditos y KYC', async () => {
    (global.fetch as any).mockImplementation((url: string) => {
      if (url.includes('/cuentas')) return { ok: true, json: async () => mockCuentas };
      if (url.includes('/beneficiarios')) return { ok: true, json: async () => mockBeneficiarios };
      if (url.includes('/creditos')) return { ok: true, json: async () => mockCreditos };
      if (url.includes('/kyc')) return { ok: true, json: async () => mockKycAprobado };
      return { ok: true, json: async () => mockSocio };
    });

    render(<SocioDetallePage />);

    await waitFor(() => {
      expect(screen.getByRole('tab', { name: /cuentas/i })).toBeTruthy();
      expect(screen.getByRole('tab', { name: /beneficiarios/i })).toBeTruthy();
      expect(screen.getByRole('tab', { name: /créditos/i })).toBeTruthy();
      expect(screen.getByRole('tab', { name: /kyc/i })).toBeTruthy();
    });
  });

  it('debe mostrar badge de estado del socio', async () => {
    (global.fetch as any).mockImplementation((url: string) => {
      if (url.includes('/cuentas')) return { ok: true, json: async () => mockCuentas };
      if (url.includes('/beneficiarios')) return { ok: true, json: async () => mockBeneficiarios };
      if (url.includes('/creditos')) return { ok: true, json: async () => mockCreditos };
      if (url.includes('/kyc')) return { ok: true, json: async () => mockKycAprobado };
      return { ok: true, json: async () => mockSocio };
    });

    render(<SocioDetallePage />);

    await waitFor(() => {
      expect(screen.getByText('ACTIVO')).toBeTruthy();
    });
  });

  it('debe mostrar botón de desactivar', async () => {
    (global.fetch as any).mockImplementation((url: string) => {
      if (url.includes('/cuentas')) return { ok: true, json: async () => mockCuentas };
      if (url.includes('/beneficiarios')) return { ok: true, json: async () => mockBeneficiarios };
      if (url.includes('/creditos')) return { ok: true, json: async () => mockCreditos };
      if (url.includes('/kyc')) return { ok: true, json: async () => mockKycAprobado };
      return { ok: true, json: async () => mockSocio };
    });

    render(<SocioDetallePage />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /desactivar/i })).toBeTruthy();
    });
  });
});