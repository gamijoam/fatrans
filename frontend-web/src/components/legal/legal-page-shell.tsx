import Link from 'next/link';
import { ChevronLeft, AlertTriangle } from 'lucide-react';

/**
 * Shell común para páginas legales (T&C, LOPDP, cookies) — issue #205.
 *
 * Estructura: header con logo + back a Home, banner de borrador (visible
 * mientras el contenido no haya pasado revisión legal externa), main con
 * el contenido pasado como children, footer minimal con fecha y versión.
 *
 * NOTA: el banner `borrador` debe permanecer hasta que un abogado valide
 * el texto. Cuando se reemplace el contenido por la versión definitiva,
 * eliminar la prop `borrador` (o ponerla en false) y subir la versión.
 */
export interface LegalPageShellProps {
  /** Título visible en el header (ej. "Términos y Condiciones"). */
  titulo: string;
  /** Subtítulo opcional bajo el título. */
  subtitulo?: string;
  /** Versión del documento (se muestra en el footer). */
  version: string;
  /** Fecha de última actualización, formato ISO o legible. */
  ultimaActualizacion: string;
  /**
   * Si `true`, muestra el banner amarillo "BORRADOR — REQUIERE REVISIÓN LEGAL".
   * Mantenerlo en `true` hasta que un abogado valide y firme el contenido.
   */
  borrador?: boolean;
  children: React.ReactNode;
}

export function LegalPageShell({
  titulo,
  subtitulo,
  version,
  ultimaActualizacion,
  borrador = true,
  children,
}: LegalPageShellProps) {
  return (
    <div className="min-h-screen bg-slate-50">
      {/* Header */}
      <header className="bg-white border-b border-slate-200">
        <div className="max-w-4xl mx-auto px-6 py-4 flex items-center justify-between">
          <Link
            href="/"
            className="inline-flex items-center gap-2 text-sm text-slate-600 hover:text-[#0F2744] transition-colors"
          >
            <ChevronLeft className="w-4 h-4" />
            Volver al inicio
          </Link>
          <span className="text-sm font-semibold text-[#0F2744]">Fatrans</span>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-6 py-10">
        {/* Banner de borrador — visible hasta validación legal */}
        {borrador && (
          <div
            role="alert"
            className="mb-8 flex items-start gap-3 rounded-lg border border-amber-300 bg-amber-50 p-4"
          >
            <AlertTriangle className="w-5 h-5 text-amber-600 flex-shrink-0 mt-0.5" />
            <div className="text-sm text-amber-900">
              <p className="font-semibold">Documento en revisión legal</p>
              <p className="mt-1 leading-relaxed">
                Este texto es un borrador estructurado para fines de
                desarrollo y QA. Debe ser revisado, ajustado y firmado por
                un abogado especialista en derecho financiero y de
                protección de datos venezolano antes de pasar a producción.
                Hasta entonces, su aceptación no constituye un contrato
                jurídicamente vinculante en su forma final.
              </p>
            </div>
          </div>
        )}

        {/* Título */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-[#0F2744]">{titulo}</h1>
          {subtitulo && (
            <p className="text-base text-slate-600 mt-2">{subtitulo}</p>
          )}
          <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-500">
            <span>Versión {version}</span>
            <span aria-hidden="true">·</span>
            <span>Última actualización: {ultimaActualizacion}</span>
          </div>
        </div>

        {/* Contenido: estilos via selectores arbitrarios para evitar añadir
            la dependencia @tailwindcss/typography por 3 páginas. */}
        <article
          className={[
            'max-w-none text-slate-700',
            '[&_h2]:text-xl [&_h2]:font-semibold [&_h2]:text-[#0F2744] [&_h2]:mt-8 [&_h2]:mb-3',
            '[&_h3]:text-base [&_h3]:font-semibold [&_h3]:text-[#0F2744] [&_h3]:mt-5 [&_h3]:mb-2',
            '[&_p]:leading-relaxed [&_p]:my-3',
            '[&_ul]:list-disc [&_ul]:pl-6 [&_ul]:my-3 [&_ul]:space-y-1',
            '[&_li]:leading-relaxed',
            '[&_strong]:text-[#0F2744] [&_strong]:font-semibold',
          ].join(' ')}
        >
          {children}
        </article>
      </main>

      {/* Footer minimal */}
      <footer className="mt-16 border-t border-slate-200 bg-white">
        <div className="max-w-4xl mx-auto px-6 py-6 text-xs text-slate-500 flex flex-wrap items-center justify-between gap-3">
          <span>© {new Date().getFullYear()} Fatrans · Documento {version}</span>
          <div className="flex gap-4">
            <Link href="/terminos" className="hover:text-[#0F2744]">Términos</Link>
            <Link href="/lopdp" className="hover:text-[#0F2744]">LOPDP</Link>
            <Link href="/cookies" className="hover:text-[#0F2744]">Cookies</Link>
          </div>
        </div>
      </footer>
    </div>
  );
}
