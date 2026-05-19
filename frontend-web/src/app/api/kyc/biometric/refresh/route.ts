import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * BFF: pide al backend que sincronice el estado biométrico con el proveedor
 * (Didit) y devuelve el estado actual. Usado por el componente
 * `BiometricCapture` en polling cada 5s mientras la verificación está en
 * progreso — suple al webhook cuando éste no está configurado o tarda.
 *
 * Idempotente: si el intento ya está en estado final, el backend devuelve sin
 * llamar al proveedor.
 */
export async function POST(request: NextRequest) {
  const token = request.cookies.get('access_token')?.value;
  if (!token) {
    return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
  }

  // `cache: 'no-store'` por defensa en profundidad. POSTs no se cachean
  // por default, pero Next.js a veces hace cosas raras con server fetch.
  const backendResponse = await fetch(`${BACKEND_URL}/api/v1/kyc/biometric/refresh`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token.replace(/^"|"$/g, '')}`,
    },
    cache: 'no-store',
  });

  // No traducimos 5xx a 502 acá: el componente está en polling, queremos
  // que vea el error real y decida si sigue intentando o muestra fallback.
  if (!backendResponse.ok) {
    const errorData = await backendResponse.json().catch(() => ({}));
    return NextResponse.json(
      { message: errorData.message || 'No pudimos sincronizar la verificación' },
      { status: backendResponse.status, headers: noCacheHeaders() },
    );
  }

  const data = await backendResponse.json();
  // No-cache headers en defense in depth — POSTs no se cachean por default
  // pero algunos intermediarios o service workers pueden romper esa regla.
  return NextResponse.json(data, { status: 200, headers: noCacheHeaders() });
}

/**
 * Headers anti-cache para responses con estado volátil — mismo patrón que
 * /api/auth/me y /api/kyc/estado. Sin esto el frontend puede ver respuestas
 * viejas durante el polling de estado biométrico.
 */
function noCacheHeaders(): Record<string, string> {
  return {
    'Cache-Control': 'no-store, no-cache, must-revalidate, proxy-revalidate',
    Pragma: 'no-cache',
    Expires: '0',
  };
}
