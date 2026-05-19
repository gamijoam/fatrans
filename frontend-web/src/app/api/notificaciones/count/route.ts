import { NextRequest, NextResponse } from 'next/server';

// `export const dynamic = 'force-dynamic'` desactiva el cache automático de
// fetch() del lado del server en Next.js 14. Sin esto, los GET internos del BFF
// al backend Java se cachean por URL+headers, devolviendo respuestas viejas tras
// mutaciones (caso real Ronni QA 19-may-2026: KYC aprobado en BD pero el admin
// veía la solicitud aún en la cola).
export const dynamic = 'force-dynamic';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * BFF para /api/v1/notificaciones/count (issue #214 PR-B).
 *
 * Endpoint ligero para el badge del NotificationBell. El frontend hace
 * polling cada 30s sobre esto — debe ser barato y rápido.
 *
 * No cacheable: el badge debe reflejar cambios casi-realtime.
 */
export async function GET(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    if (!accessToken) {
      // Para el polling, devolvemos 0 sin auth en vez de 401 — evita ruido
      // en consola del browser cuando expira la sesión. El Bell se ocultará
      // de todas formas (no hay usuario en el store).
      return NextResponse.json({ noLeidas: 0 }, { status: 200 });
    }

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/notificaciones/count`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      }
    );

    if (!backendResponse.ok) {
      // Fallar abierto: devolver 0 en vez de error — el badge se oculta
      // pero no rompe la UI.
      return NextResponse.json({ noLeidas: 0 }, { status: 200 });
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, {
      status: 200,
      headers: { 'Cache-Control': 'no-store' },
    });
  } catch (error) {
    console.error('Notificaciones count error:', error);
    return NextResponse.json({ noLeidas: 0 }, { status: 200 });
  }
}
