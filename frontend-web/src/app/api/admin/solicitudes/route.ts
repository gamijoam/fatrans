import { NextRequest, NextResponse } from 'next/server';
import { validateAdminAccess } from '@/lib/auth/admin-validation';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function GET(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token');
    const authResult = validateAdminAccess({ accessToken: accessToken?.value });
    if (!authResult.valid) {
      return NextResponse.json({ message: authResult.message }, { status: authResult.status });
    }
    const token = accessToken!.value;

    const { searchParams } = new URL(request.url);
    const estado = searchParams.get('estado') || 'PENDIENTE';
    const pagina = searchParams.get('pagina') || '0';

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/socios/solicitudes?estado=${estado}&page=${pagina}&size=10`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
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
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}