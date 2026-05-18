import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * BFF para POST /api/v1/notificaciones/marcar-todas-leidas (issue #214 PR-B).
 *
 * Marca todas las notificaciones no-leídas del usuario actual.
 */
export async function POST(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/notificaciones/marcar-todas-leidas`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      }
    );

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Error al marcar todas como leídas' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });
  } catch (error) {
    console.error('Marcar todas leídas error:', error);
    return NextResponse.json({ message: 'Error interno' }, { status: 500 });
  }
}
