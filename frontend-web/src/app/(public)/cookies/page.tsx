import type { Metadata } from 'next';
import { LegalPageShell } from '@/components/legal/legal-page-shell';

export const metadata: Metadata = {
  title: 'Política de Cookies | Fatrans',
  description:
    'Información sobre las cookies utilizadas en la plataforma Fatrans y cómo gestionarlas.',
};

/**
 * Página /cookies (issue #205) — Política de Cookies.
 *
 * Incluye una tabla con las cookies efectivamente utilizadas (sesión,
 * preferencias, anti-CSRF). Si en el futuro se incorporan cookies de
 * analítica o marketing, esta tabla debe actualizarse y el banner de
 * consentimiento granular debe permitir activarlas/desactivarlas
 * (alcance del issue #218 — banner de cookies).
 */
export default function CookiesPage() {
  return (
    <LegalPageShell
      titulo="Política de Cookies"
      subtitulo="Qué cookies usamos en la plataforma Fatrans y cómo puedes gestionarlas"
      version="1.0-borrador"
      ultimaActualizacion="17 de mayo de 2026"
      borrador
    >
      <h2>1. ¿Qué es una cookie?</h2>
      <p>
        Una cookie es un pequeño archivo de texto que un sitio web guarda
        en su navegador para recordar información sobre su visita: idioma
        preferido, sesión iniciada, configuración, etc. Las cookies son
        útiles para que la plataforma funcione correctamente y para mejorar
        su experiencia.
      </p>

      <h2>2. Tipos de cookies según su finalidad</h2>
      <ul>
        <li>
          <strong>Estrictamente necesarias:</strong> imprescindibles para
          que la plataforma funcione (sesión, seguridad). No requieren
          consentimiento del usuario.
        </li>
        <li>
          <strong>Preferencias:</strong> recuerdan opciones como idioma o
          tema visual.
        </li>
        <li>
          <strong>Analíticas:</strong> miden uso de la plataforma de forma
          anónima y agregada (no usadas actualmente).
        </li>
        <li>
          <strong>Marketing:</strong> usadas para personalizar publicidad
          (no usadas actualmente).
        </li>
      </ul>

      <h2>3. Cookies utilizadas por Fatrans</h2>
      <p>
        Esta es la lista completa de cookies activas en la plataforma a la
        fecha de esta política:
      </p>

      <div className="overflow-x-auto">
        <table className="w-full text-sm border-collapse my-4">
          <thead>
            <tr className="bg-slate-100 text-[#0F2744]">
              <th className="border border-slate-200 px-3 py-2 text-left font-semibold">Cookie</th>
              <th className="border border-slate-200 px-3 py-2 text-left font-semibold">Tipo</th>
              <th className="border border-slate-200 px-3 py-2 text-left font-semibold">Finalidad</th>
              <th className="border border-slate-200 px-3 py-2 text-left font-semibold">Duración</th>
            </tr>
          </thead>
          <tbody className="text-slate-700">
            <tr>
              <td className="border border-slate-200 px-3 py-2 font-mono text-xs">access_token</td>
              <td className="border border-slate-200 px-3 py-2">Necesaria</td>
              <td className="border border-slate-200 px-3 py-2">
                Mantener la sesión autenticada del usuario (HttpOnly, Secure).
              </td>
              <td className="border border-slate-200 px-3 py-2">Sesión</td>
            </tr>
            <tr className="bg-slate-50">
              <td className="border border-slate-200 px-3 py-2 font-mono text-xs">refresh_token</td>
              <td className="border border-slate-200 px-3 py-2">Necesaria</td>
              <td className="border border-slate-200 px-3 py-2">
                Renovar el token de acceso sin pedir credenciales nuevamente (HttpOnly, Secure).
              </td>
              <td className="border border-slate-200 px-3 py-2">7 días</td>
            </tr>
            <tr>
              <td className="border border-slate-200 px-3 py-2 font-mono text-xs">XSRF-TOKEN</td>
              <td className="border border-slate-200 px-3 py-2">Necesaria</td>
              <td className="border border-slate-200 px-3 py-2">
                Protección contra ataques CSRF en operaciones que modifican
                datos.
              </td>
              <td className="border border-slate-200 px-3 py-2">Sesión</td>
            </tr>
            <tr className="bg-slate-50">
              <td className="border border-slate-200 px-3 py-2 font-mono text-xs">theme</td>
              <td className="border border-slate-200 px-3 py-2">Preferencias</td>
              <td className="border border-slate-200 px-3 py-2">
                Recordar el tema visual elegido por el usuario.
              </td>
              <td className="border border-slate-200 px-3 py-2">1 año</td>
            </tr>
          </tbody>
        </table>
      </div>

      <p className="text-sm text-slate-500">
        Fatrans <strong>no utiliza</strong> actualmente cookies de
        analítica, marketing ni de terceros (Google Analytics, Meta Pixel,
        etc.). Si en el futuro se incorporan, esta política será actualizada
        y se solicitará consentimiento expreso mediante el banner de
        cookies.
      </p>

      <h2>4. Gestión de cookies</h2>
      <p>
        Puede aceptar o rechazar las cookies no esenciales desde el banner
        que aparece en su primera visita. También puede gestionarlas
        directamente desde su navegador:
      </p>
      <ul>
        <li>
          <strong>Chrome:</strong> Configuración → Privacidad y seguridad →
          Cookies y otros datos de sitios.
        </li>
        <li>
          <strong>Firefox:</strong> Opciones → Privacidad &amp; Seguridad →
          Cookies y datos del sitio.
        </li>
        <li>
          <strong>Safari:</strong> Preferencias → Privacidad → Gestionar
          datos de sitios web.
        </li>
        <li>
          <strong>Edge:</strong> Configuración → Cookies y permisos del
          sitio.
        </li>
      </ul>
      <p>
        Tenga en cuenta que <strong>desactivar las cookies estrictamente
        necesarias impedirá el uso de la plataforma</strong> (no podrá
        iniciar sesión).
      </p>

      <h2>5. Cookies de terceros</h2>
      <p>
        Actualmente Fatrans no carga cookies de terceros. Cualquier cambio
        en este sentido será comunicado y publicado en esta misma política.
      </p>

      <h2>6. Cambios a esta política</h2>
      <p>
        Esta política puede actualizarse cuando agreguemos o retiremos
        cookies. La fecha de "Última actualización" en la parte superior de
        esta página refleja la versión vigente.
      </p>

      <h2>7. Contacto</h2>
      <p>
        Para cualquier consulta sobre el uso de cookies, escriba a{' '}
        <a href="mailto:dpo@fatrans.com.ve" className="text-[#16A34A] hover:underline">
          dpo@fatrans.com.ve
        </a>
        .
      </p>
    </LegalPageShell>
  );
}
