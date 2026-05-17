'use client';

import { useEffect, useState } from 'react';

/**
 * Lee el umbral LOCDOFT vigente desde el backend (#218 PR-C).
 *
 * Pre-validación: el form puede preguntar "¿este monto supera el umbral?"
 * antes de mostrar el modal, para mejor UX. Si el endpoint falla o devuelve
 * null (fail-open en backend), el hook devuelve `null` y el frontend
 * trata como "sin umbral" — el backend igualmente devolverá 422 si la
 * operación lo requiere, así que es defensa en profundidad.
 *
 * Se cachea en memoria del componente (se vuelve a pedir en cada mount).
 * Si en el futuro queremos cachear entre sesiones, se mueve a SWR/React
 * Query con su propio storage.
 */
export function useLocdoftUmbral(moneda: 'VES' | 'USD' = 'VES') {
  const [umbral, setUmbral] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelado = false;
    setLoading(true);
    fetch(`/api/compliance/locdoft/umbral?moneda=${encodeURIComponent(moneda)}`)
      .then((r) => (r.ok ? r.json() : null))
      .then((data) => {
        if (cancelado) return;
        // backend devuelve { moneda, umbral }. Si umbral viene null, fail-open.
        const valor = data?.umbral != null ? Number(data.umbral) : null;
        setUmbral(Number.isFinite(valor as number) ? (valor as number) : null);
      })
      .catch(() => {
        if (!cancelado) setUmbral(null); // fail-open
      })
      .finally(() => {
        if (!cancelado) setLoading(false);
      });
    return () => {
      cancelado = true;
    };
  }, [moneda]);

  return { umbral, loading };
}
