import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function GET(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const socioId = searchParams.get('socioId');
    const tipo = searchParams.get('tipo');
    const estado = searchParams.get('estado');
    const page = searchParams.get('page') || '0';
    const size = searchParams.get('size') || '20';

    if (!socioId) {
      return NextResponse.json({ message: 'socioId requerido' }, { status: 400 });
    }

    const queryParams = new URLSearchParams({
      page,
      size,
    });
    if (tipo) queryParams.append('tipo', tipo);
    if (estado) queryParams.append('estado', estado);

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/documentos/socio/${socioId}?${queryParams.toString()}`,
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
        { message: errorData.message || 'Error al listar documentos' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });

  } catch (error) {
    console.error('Documentos list error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}