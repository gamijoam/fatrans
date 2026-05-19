import { NextRequest, NextResponse } from 'next/server';

// `export const dynamic = 'force-dynamic'` desactiva el cache automático de
// fetch() del lado del server en Next.js 14. Sin esto, los GET internos del BFF
// al backend Java se cachean por URL+headers, devolviendo respuestas viejas tras
// mutaciones (caso real Ronni QA 19-may-2026: KYC aprobado en BD pero el admin
// veía la solicitud aún en la cola).
export const dynamic = 'force-dynamic';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function GET(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const socioId = searchParams.get('socioId');

    if (!socioId) {
      return NextResponse.json({ message: 'socioId requerido' }, { status: 400 });
    }

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/socios/${socioId}/beneficiarios`,
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
      // Backend retorna error cuando no hay beneficiarios — tratar como lista vacía
      if (errorData.code === 'BENEFICIARIO_NO_ENCONTRADO' || backendResponse.status === 404) {
        return NextResponse.json({ beneficiarios: [], total: 0 }, { status: 200 });
      }
      return NextResponse.json(
        { message: errorData.message || 'Error al obtener beneficiarios' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });

  } catch (error) {
    console.error('Beneficiarios list error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}

export async function POST(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    const body = await request.json();
    const { socioId, ...beneficiarioData } = body;

    if (!socioId) {
      return NextResponse.json({ message: 'socioId requerido' }, { status: 400 });
    }

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/socios/${socioId}/beneficiarios`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(beneficiarioData),
      }
    );

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Error al crear beneficiario' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 201 });

  } catch (error) {
    console.error('Beneficiarios create error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}
