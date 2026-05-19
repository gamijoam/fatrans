import { NextRequest, NextResponse } from 'next/server';
import jwt from 'jsonwebtoken';

// `export const dynamic = 'force-dynamic'` desactiva el cache automático de
// fetch() del lado del server en Next.js 14. Sin esto, los GET internos del BFF
// al backend Java se cachean por URL+headers, devolviendo respuestas viejas tras
// mutaciones (caso real Ronni QA 19-may-2026: KYC aprobado en BD pero el admin
// veía la solicitud aún en la cola).
export const dynamic = 'force-dynamic';

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
