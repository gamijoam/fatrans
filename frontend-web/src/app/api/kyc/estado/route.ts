import { NextRequest, NextResponse } from 'next/server';

// `export const dynamic = 'force-dynamic'` desactiva el cache automático de
// fetch() del lado del server en Next.js 14. Sin esto, los GET internos del BFF
// al backend Java se cachean por URL+headers, devolviendo respuestas viejas tras
// mutaciones (caso real Ronni QA 19-may-2026: KYC aprobado en BD pero el admin
// veía la solicitud aún en la cola).
export const dynamic = 'force-dynamic';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * Headers anti-cache para responses con estado volátil. Patrón aplicado
 * también en /api/auth/me — busca evitar que el navegador o intermediarios
 * sirvan respuestas viejas mientras hay polling activo de cambios de estado
 * (KYC biométrico, debeCambiarPassword, etc).
 */
function noCacheHeaders(): Record<string, string> {
  return {
    'Cache-Control': 'no-store, no-cache, must-revalidate, proxy-revalidate',
    Pragma: 'no-cache',
    Expires: '0',
  };
}

export async function GET(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    // `cache: 'no-store'` evita que Next.js 14 cachee la respuesta del
    // backend en el server side (el cache de Next.js es por URL+headers y
    // se comparte entre requests). Sin esto, el polling de KYC sirve el
    // estado del primer GET (NO_INICIADA/EN_PROGRESO) eternamente, aunque
    // el backend ya haya cambiado a APROBADA.
    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/kyc/estado`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        cache: 'no-store',
      }
    );

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      // Backend retorna 500 + KYC_000 cuando el socio no tiene KYC iniciado
      if (errorData.error === 'KYC_000' || backendResponse.status === 500) {
        return NextResponse.json(
          { estado: 'SIN_KYC', mensaje: 'No has iniciado el proceso KYC' },
          {
            status: 200,
            headers: noCacheHeaders(),
          },
        );
      }
      return NextResponse.json(
        { message: errorData.message || 'Error al obtener estado KYC' },
        { status: backendResponse.status, headers: noCacheHeaders() },
      );
    }

    const data = await backendResponse.json();
    // No-cache: el frontend polea este endpoint mientras la verificación
    // biométrica está en progreso. Sin estos headers, el navegador devuelve
    // la respuesta cacheada del primer load (estadoBiometria=NO_INICIADA) y
    // el polling nunca "ve" la transición a APROBADA aunque el backend la haya
    // persistido (caso real Gabriel QA 19-may-2026).
    return NextResponse.json(data, {
      status: 200,
      headers: noCacheHeaders(),
    });

  } catch (error) {
    console.error('KYC estado error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}
