import { NextRequest, NextResponse } from 'next/server';

// `export const dynamic = 'force-dynamic'` desactiva el cache automático de
// fetch() del lado del server en Next.js 14. Sin esto, los GET internos del BFF
// al backend Java se cachean por URL+headers, devolviendo respuestas viejas tras
// mutaciones (caso real Ronni QA 19-may-2026: KYC aprobado en BD pero el admin
// veía la solicitud aún en la cola).
export const dynamic = 'force-dynamic';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function GET(
  request: NextRequest,
  { params }: { params: { numeroCuenta: string } }
) {
  try {
    const accessToken = request.cookies.get('access_token');

    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/cuentas/${params.numeroCuenta}`,
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
        { message: errorData.message || 'Error al consultar cuenta' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });

  } catch (error) {
    console.error('Cuenta error:', error);
    return NextResponse.json({ message: 'Error al consultar cuenta' }, { status: 500 });
  }
}
