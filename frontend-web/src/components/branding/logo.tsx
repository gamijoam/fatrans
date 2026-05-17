'use client';

import Image from 'next/image';

/**
 * Logo institucional de Fatrans.
 *
 * Usa `next/image` para optimización automática (resize, lazy, formatos
 * modernos). El archivo fuente vive en `frontend-web/public/logo-fatrans.svg`
 * (placeholder).
 *
 * Para reemplazar por el logo institucional real (PNG render del logo
 * metálico con la mascota digital):
 *   1. Guardar el archivo en `frontend-web/public/logo-fatrans.png`
 *      (PNG con fondo transparente, idealmente 1024×1024).
 *   2. Cambiar el `src` abajo de `.svg` a `.png` (Next.js detecta el
 *      formato automáticamente).
 *   3. Opcional: eliminar `logo-fatrans.svg` si ya no se necesita.
 */

export interface LogoProps {
  /** Tamaño en px. Default 96. El componente mantiene la proporción cuadrada. */
  size?: number;
  /** Clase extra para el wrapper. */
  className?: string;
  /**
   * Si true, NO muestra el subtítulo "Asociación de Ahorro y Crédito".
   * Útil cuando el logo ya va dentro de un header que tiene su propio título.
   */
  soloImagen?: boolean;
  /** Prioridad de carga (true en above-the-fold como login). */
  priority?: boolean;
}

export function Logo({ size = 96, className = '', soloImagen = false, priority = false }: LogoProps) {
  return (
    <div className={`inline-flex flex-col items-center gap-2 ${className}`}>
      <div
        className="relative"
        style={{ width: size, height: size }}
        aria-label="Fatrans — Asociación de Ahorro y Crédito"
      >
        <Image
          src="/logo-fatrans.svg"
          alt="Fatrans"
          fill
          sizes={`${size}px`}
          priority={priority}
          className="object-contain drop-shadow-md"
        />
      </div>
      {!soloImagen && (
        <div className="text-center select-none">
          <p className="text-[10px] uppercase tracking-[0.2em] text-slate-500 font-semibold">
            Asociación de Ahorro y Crédito
          </p>
        </div>
      )}
    </div>
  );
}
