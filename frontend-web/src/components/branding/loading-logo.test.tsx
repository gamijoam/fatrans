import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { LoadingLogo } from '@/components/branding/loading-logo';
import { Logo } from '@/components/branding/logo';

// next/image necesita mock en jsdom (no soporta el componente)
vi.mock('next/image', () => ({
  default: ({ alt, ...props }: { alt: string; [k: string]: unknown }) => {
    // eslint-disable-next-line @next/next/no-img-element, jsx-a11y/alt-text
    return <img alt={alt} {...(props as object)} />;
  },
}));

describe('Logo', () => {
  it('renderiza con alt="Fatrans"', () => {
    render(<Logo />);
    expect(screen.getByAltText('Fatrans')).toBeTruthy();
  });

  it('respeta el tamaño custom', () => {
    const { container } = render(<Logo size={200} />);
    const wrapper = container.querySelector('[aria-label*="Fatrans"]');
    expect(wrapper).toBeTruthy();
    expect((wrapper as HTMLElement).style.width).toBe('200px');
  });

  it('por defecto muestra el subtítulo "Asociación de Ahorro y Crédito"', () => {
    render(<Logo />);
    expect(screen.getByText(/Asociación de Ahorro y Crédito/i)).toBeTruthy();
  });

  it('soloImagen oculta el subtítulo', () => {
    render(<Logo soloImagen />);
    expect(screen.queryByText(/Asociación de Ahorro y Crédito/i)).toBeNull();
  });
});

describe('LoadingLogo', () => {
  it('por defecto muestra mensaje "Iniciando sesión..."', () => {
    render(<LoadingLogo />);
    expect(screen.getByText(/Iniciando sesión/i)).toBeTruthy();
  });

  it('acepta mensaje custom', () => {
    render(<LoadingLogo mensaje="Cargando datos del socio" />);
    expect(screen.getByText('Cargando datos del socio')).toBeTruthy();
  });

  it('omite el mensaje cuando se pasa string vacío', () => {
    render(<LoadingLogo mensaje="" />);
    // El mensaje vacío hace que el <p> no se renderice
    expect(screen.queryByRole('status')).toBeNull();
  });

  it('variante "overlay" envuelve en contenedor full-screen con role="status"', () => {
    render(<LoadingLogo variante="overlay" mensaje="Procesando..." />);
    // El overlay tiene su propio role=status con aria-label
    const overlay = screen.getByRole('status', { name: 'Procesando...' });
    expect(overlay).toBeTruthy();
    expect(overlay.className).toContain('fixed');
    expect(overlay.className).toContain('z-[200]');
  });

  it('variante "inline" NO envuelve en overlay full-screen', () => {
    const { container } = render(<LoadingLogo variante="inline" />);
    expect(container.querySelector('.fixed.inset-0')).toBeNull();
  });
});
