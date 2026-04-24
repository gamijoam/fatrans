import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

function getSocioIdFromToken(accessToken: string): string | null {
  try {
    const payload = accessToken.split('.')[1];
    const decoded = Buffer.from(payload, 'base64').toString('utf-8');
    const data = JSON.parse(decoded);
    return data.socio_id || data.socioId || null;
  } catch {
    return null;
  }
}

export async function GET(
  request: NextRequest,
  { params }: { params: { socioId: string } }
) {
  try {
    const accessToken = request.cookies.get('access_token');

    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    const tokenSocioId = getSocioIdFromToken(accessToken.value);
    if (!tokenSocioId) {
      return NextResponse.json({ message: 'Token inválido' }, { status: 401 });
    }

    if (tokenSocioId !== params.socioId) {
      return NextResponse.json(
        { message: 'No autorizado para acceder a estos datos' },
        { status: 403 }
      );
    }

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/creditos/solicitudes/socio/${params.socioId}`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${accessToken.value}`,
          'Content-Type': 'application/json'
        },
        credentials: 'include',
      }
    );

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Error al obtener solicitudes' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });

  } catch (error) {
    console.error('Solicitudes error:', error);
    return NextResponse.json({ message: 'Error al obtener solicitudes' }, { status: 500 });
  }
}