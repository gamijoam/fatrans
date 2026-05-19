import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { CookieConsentBanner } from '@/components/legal/cookie-consent-banner';
import { leerCookieConsent } from '@/lib/utils/cookie-consent-storage';
import { useAuthStore } from '@/stores/auth-store';

/**
 * Tests del banner de consentimiento de cookies (issue #218 PR-A).
 *
 * Cubre: visibilidad inicial, decisiones rápidas (aceptar/rechazar),
 * panel personalizado con toggles granulares, persistencia, y que el
 * banner desaparece tras una decisión.
 */

describe('CookieConsentBanner', () => {
  beforeEach(() => {
    window.localStorage.clear();
    // Reset auth store entre tests para evitar leak del flag debeCambiarPassword.
    useAuthStore.setState({ user: null, isAuthenticated: false, isLoading: false });
  });

  it('NO aparece cuando el usuario tiene debeCambiarPassword=true (modal obligatorio abierto, evita conflicto z-index reportado en PROD 19-may-2026)', async () => {
    // Sin preferencia guardada → normalmente el banner aparecería
    useAuthStore.setState({
      user: {
        id: 'user-1',
        nombreUsuario: 'socio_prueba',
        correoElectronico: 'socio@test.com',
        nombreCompleto: 'Socio Prueba',
        rol: 'SOCIO',
        socioId: 'socio-1',
        debeCambiarPassword: true,
      },
      isAuthenticated: true,
      isLoading: false,
    });

    render(<CookieConsentBanner />);
    await new Promise((r) => setTimeout(r, 50));

    // El banner debe permanecer oculto porque el modal de cambio de
    // password está sobre la pantalla — su z-index entra en conflicto
    // con el del banner y bloquea ambos. El socio ya aceptó cookies en
    // el form de registro (LOPDP); el banner es informativo y puede esperar.
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('aparece cuando no hay preferencia guardada', async () => {
    render(<CookieConsentBanner />);
    await waitFor(() => {
      expect(screen.getByRole('dialog')).toBeTruthy();
    });
    expect(screen.getByText(/Esta plataforma usa cookies/i)).toBeTruthy();
  });

  it('NO aparece cuando ya hay preferencia guardada', async () => {
    window.localStorage.setItem(
      'fatrans.cookie.consent',
      JSON.stringify({
        version: '1',
        necesarias: true,
        preferencias: true,
        analiticas: true,
        marketing: true,
        fechaConsentimiento: '2026-05-17T22:00:00Z',
      }),
    );
    render(<CookieConsentBanner />);
    // Damos un tick al efecto. Si no aparece, el queryByRole es null.
    await new Promise((r) => setTimeout(r, 50));
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('clic en "Aceptar todas" persiste todas las categorías y oculta el banner', async () => {
    render(<CookieConsentBanner />);
    await waitFor(() => screen.getByRole('dialog'));
    fireEvent.click(screen.getByRole('button', { name: /aceptar todas/i }));
    await waitFor(() => {
      expect(screen.queryByRole('dialog')).toBeNull();
    });
    const stored = leerCookieConsent();
    expect(stored).not.toBeNull();
    expect(stored!.analiticas).toBe(true);
    expect(stored!.marketing).toBe(true);
    expect(stored!.preferencias).toBe(true);
  });

  it('clic en "Solo necesarias" persiste opcionales en false y oculta el banner', async () => {
    render(<CookieConsentBanner />);
    await waitFor(() => screen.getByRole('dialog'));
    fireEvent.click(screen.getByRole('button', { name: /solo necesarias/i }));
    await waitFor(() => {
      expect(screen.queryByRole('dialog')).toBeNull();
    });
    const stored = leerCookieConsent();
    expect(stored).not.toBeNull();
    expect(stored!.necesarias).toBe(true); // siempre true
    expect(stored!.preferencias).toBe(false);
    expect(stored!.analiticas).toBe(false);
    expect(stored!.marketing).toBe(false);
  });

  it('"Personalizar" expande el panel con toggles granulares', async () => {
    render(<CookieConsentBanner />);
    await waitFor(() => screen.getByRole('dialog'));
    fireEvent.click(screen.getByRole('button', { name: /personalizar/i }));
    expect(screen.getByText(/Preferencias de cookies/i)).toBeTruthy();
    // Las 4 categorías aparecen
    expect(screen.getByText(/^Necesarias$/i)).toBeTruthy();
    expect(screen.getByText(/^Preferencias$/i)).toBeTruthy();
    expect(screen.getByText(/^Analíticas$/i)).toBeTruthy();
    expect(screen.getByText(/^Marketing$/i)).toBeTruthy();
  });

  it('en el panel expandido, la categoría "Necesarias" está disabled (no puede desactivarse)', async () => {
    render(<CookieConsentBanner />);
    await waitFor(() => screen.getByRole('dialog'));
    fireEvent.click(screen.getByRole('button', { name: /personalizar/i }));
    const checkboxNecesarias = screen.getByRole('checkbox', { name: /aceptar cookies necesarias/i });
    expect(checkboxNecesarias.hasAttribute('disabled')).toBe(true);
    expect((checkboxNecesarias as HTMLInputElement).checked).toBe(true);
  });

  it('guardar selección personalizada persiste exactamente los toggles seleccionados', async () => {
    render(<CookieConsentBanner />);
    await waitFor(() => screen.getByRole('dialog'));
    fireEvent.click(screen.getByRole('button', { name: /personalizar/i }));
    // Por defecto: preferencias=true, analíticas=false, marketing=false
    // Activo analíticas, dejo marketing off, dejo preferencias on
    const cbAnaliticas = screen.getByRole('checkbox', { name: /aceptar cookies analíticas/i });
    fireEvent.click(cbAnaliticas);
    fireEvent.click(screen.getByRole('button', { name: /guardar selección/i }));
    await waitFor(() => {
      expect(screen.queryByRole('dialog')).toBeNull();
    });
    const stored = leerCookieConsent();
    expect(stored!.preferencias).toBe(true);
    expect(stored!.analiticas).toBe(true);
    expect(stored!.marketing).toBe(false);
  });

  it('botón "Cerrar panel" colapsa de vuelta a vista compacta', async () => {
    render(<CookieConsentBanner />);
    await waitFor(() => screen.getByRole('dialog'));
    fireEvent.click(screen.getByRole('button', { name: /personalizar/i }));
    expect(screen.getByText(/Preferencias de cookies/i)).toBeTruthy();
    fireEvent.click(screen.getByRole('button', { name: /cerrar panel/i }));
    expect(screen.queryByText(/Preferencias de cookies/i)).toBeNull();
    expect(screen.getByText(/Esta plataforma usa cookies/i)).toBeTruthy();
  });
});
