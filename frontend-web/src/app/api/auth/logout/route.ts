import { NextRequest, NextResponse } from 'next/server';

// `export const dynamic = 'force-dynamic'` desactiva el cache automático de
// fetch() del lado del server en Next.js 14. Sin esto, los GET internos del BFF
// al backend Java se cachean por URL+headers, devolviendo respuestas viejas tras
// mutaciones (caso real Ronni QA 19-may-2026: KYC aprobado en BD pero el admin
// veía la solicitud aún en la cola).
export const dynamic = 'force-dynamic';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function POST(request: NextRequest) {
  // Sin restricción de origin/referer: el logout es seguro por sí mismo
  // (solo borra cookies — no puede hacer daño si es llamado sin sesión válida)

  try {
    const accessToken = request.cookies.get('access_token');

    const backendResponse = await fetch(`${BACKEND_URL}/api/v1/auth/logout-web`, {
      method: 'POST',
      headers: accessToken ? { 'Authorization': `Bearer ${accessToken.value}` } : {},
      credentials: 'include',
    });

    const response = NextResponse.json(
      { success: true, message: 'Sesión cerrada correctamente' },
      { status: 200 }
    );

    response.cookies.delete('access_token');

    if (backendResponse.ok) {
      const setCookieHeaders = backendResponse.headers.getSetCookie();
      setCookieHeaders.forEach((cookie) => {
        response.headers.append('Set-Cookie', cookie);
      });
    }

    return response;

  } catch (error) {
    console.error('Logout error:', error);
    return NextResponse.json(
      { message: 'Error al cerrar sesión' },
      { status: 500 }
    );
  }
}
