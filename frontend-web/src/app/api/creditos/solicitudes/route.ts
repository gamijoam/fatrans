import { NextRequest, NextResponse } from 'next/server';
import jwt from 'jsonwebtoken';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';
const JWT_SECRET = process.env.JWT_SECRET || process.env.NEXTAUTH_SECRET || 'fallback-secret-do-not-use-in-production';

function getSocioIdFromToken(accessToken: string): string | null {
  try {
    const decoded = jwt.verify(accessToken, JWT_SECRET) as { socio_id?: string; socioId?: string };
    return decoded.socio_id || decoded.socioId || null;
  } catch {
    return null;
  }
}

export async function POST(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token');

    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    const tokenSocioId = getSocioIdFromToken(accessToken.value);
    if (!tokenSocioId) {
      return NextResponse.json({ message: 'Token inválido' }, { status: 401 });
    }

    const body = await request.json();

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/creditos/solicitudes`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken.value}`,
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({
          ...body,
          canalOrigen: body.canalOrigen || 'WEB',
        }),
      }
    );

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Error al crear solicitud' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 201 });

  } catch (error) {
    console.error('CrearSolicitud error:', error);
    return NextResponse.json({ message: 'Error al crear solicitud' }, { status: 500 });
  }
}
