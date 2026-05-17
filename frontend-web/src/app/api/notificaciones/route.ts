import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * BFF para /api/v1/notificaciones del backend (issue #214 PR-B).
 *
 * GET con query params:
 *  - page (default 0)
 *  - size (default 20)
 *  - soloNoLeidas (default false) — para el dropdown del Bell
 */
export async function GET(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const page = searchParams.get('page') || '0';
    const size = searchParams.get('size') || '20';
    const soloNoLeidas = searchParams.get('soloNoLeidas') || 'false';

    const qs = new URLSearchParams({ page, size, soloNoLeidas });

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/notificaciones?${qs}`,
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
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Error al obtener notificaciones' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });
  } catch (error) {
    console.error('Notificaciones GET error:', error);
    return NextResponse.json({ message: 'Error interno' }, { status: 500 });
  }
}
