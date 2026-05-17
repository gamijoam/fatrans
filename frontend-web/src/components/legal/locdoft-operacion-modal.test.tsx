import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { LocdoftOperacionModal } from '@/components/legal/locdoft-operacion-modal';

/**
 * Tests del modal LOCDOFT para operaciones grandes (#218 PR-C).
 */

describe('LocdoftOperacionModal', () => {
  const baseProps = {
    open: true,
    tipoOperacion: 'deposito' as const,
    monto: 15000,
    moneda: 'VES' as const,
    umbral: 10000,
    onCancelar: vi.fn(),
    onConfirmar: vi.fn(),
  };

  it('no renderiza cuando open=false', () => {
    render(<LocdoftOperacionModal {...baseProps} open={false} />);
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('renderiza título, descripción LOCDOFT y monto', () => {
    render(<LocdoftOperacionModal {...baseProps} />);
    expect(screen.getByText(/Declaración jurada requerida \(LOCDOFT\)/i)).toBeTruthy();
    // El texto LOCDOFT aparece en 2 lugares (descripción + checkbox), basta con
    // verificar que aparece al menos una vez.
    expect(screen.getAllByText(/Ley Orgánica contra la Delincuencia Organizada/i).length).toBeGreaterThan(0);
    // El monto se muestra formateado con Bs
    expect(screen.getByText(/15\.000,00/)).toBeTruthy();
  });

  it('muestra el umbral vigente cuando se pasa', () => {
    render(<LocdoftOperacionModal {...baseProps} umbral={10000} />);
    expect(screen.getByText(/umbral vigente/i)).toBeTruthy();
  });

  it('omite el umbral cuando es null (fail-open)', () => {
    render(<LocdoftOperacionModal {...baseProps} umbral={null} />);
    expect(screen.queryByText(/umbral vigente/i)).toBeNull();
  });

  it('botón "Confirmar y continuar" está deshabilitado sin tildar la declaración', () => {
    render(<LocdoftOperacionModal {...baseProps} />);
    const btn = screen.getByRole('button', { name: /confirmar y continuar/i });
    expect(btn.hasAttribute('disabled')).toBe(true);
  });

  it('al tildar la declaración, el botón se habilita', () => {
    render(<LocdoftOperacionModal {...baseProps} />);
    const checkbox = screen.getByRole('checkbox', {
      name: /declaro que los fondos provienen de actividades l[íi]citas/i,
    });
    fireEvent.click(checkbox);
    const btn = screen.getByRole('button', { name: /confirmar y continuar/i });
    expect(btn.hasAttribute('disabled')).toBe(false);
  });

  it('click en Confirmar invoca onConfirmar con confirmaOrigenLicito=true y origenFondos', () => {
    const onConfirmar = vi.fn();
    render(<LocdoftOperacionModal {...baseProps} onConfirmar={onConfirmar} />);

    fireEvent.click(screen.getByRole('checkbox', { name: /declaro/i }));
    fireEvent.change(screen.getByLabelText(/origen de los fondos/i), {
      target: { value: 'Venta de vehículo' },
    });
    fireEvent.click(screen.getByRole('button', { name: /confirmar y continuar/i }));

    expect(onConfirmar).toHaveBeenCalledWith({
      confirmaOrigenLicito: true,
      origenFondos: 'Venta de vehículo',
    });
  });

  it('click en Cancelar invoca onCancelar', () => {
    const onCancelar = vi.fn();
    render(<LocdoftOperacionModal {...baseProps} onCancelar={onCancelar} />);
    fireEvent.click(screen.getByRole('button', { name: /cancelar/i }));
    expect(onCancelar).toHaveBeenCalled();
  });

  it('verbo cambia según tipoOperacion (deposito → "depositar", retiro → "retirar")', () => {
    const { unmount } = render(<LocdoftOperacionModal {...baseProps} tipoOperacion="deposito" />);
    expect(screen.getByText(/depositar/i)).toBeTruthy();
    unmount();

    render(<LocdoftOperacionModal {...baseProps} tipoOperacion="retiro" />);
    expect(screen.getByText(/retirar/i)).toBeTruthy();
  });

  it('moneda USD muestra "$" en lugar de "Bs"', () => {
    render(<LocdoftOperacionModal {...baseProps} moneda="USD" monto={1500} />);
    expect(screen.getByText(/\$ 1\.500,00/)).toBeTruthy();
  });

  it('procesando=true deshabilita Cancelar y Confirmar', () => {
    render(<LocdoftOperacionModal {...baseProps} procesando />);
    expect(screen.getByRole('button', { name: /cancelar/i }).hasAttribute('disabled')).toBe(true);
    // Confirmar está disabled por checkbox no marcado + por procesando
    expect(screen.getByRole('button', { name: /procesando/i }).hasAttribute('disabled')).toBe(true);
  });
});
