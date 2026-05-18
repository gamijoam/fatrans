import { NextRequest, NextResponse } from 'next/server';
import { validateAdminAccess } from '@/lib/auth/admin-validation';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

// Next.js 14 cachea route handlers GET por defecto. Forzamos dynamic +
// `cache: 'no-store'` en el fetch para que cada cambio de filtro del admin
// dispare una nueva request al backend, no devuelva resultados viejos.
export const dynamic = 'force-dynamic';

export async function GET(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    const authResult = validateAdminAccess({ accessToken });
    if (!authResult.valid) {
      return NextResponse.json({ message: authResult.message }, { status: authResult.status });
    }

    const { searchParams } = new URL(request.url);
    const page = searchParams.get('page') || '0';
    const size = searchParams.get('size') || '10';
    const estado = searchParams.get('estado') || 'EN_REVISION';
    const nivel = searchParams.get('nivel');

    const params = new URLSearchParams();
    params.set('page', page);
    params.set('size', size);
    params.set('estado', estado);
    if (nivel) params.set('nivel', nivel);

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/kyc/cola-revision?${params.toString()}`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        cache: 'no-store',
      }
    );

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Error al obtener cola de revisión' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });

  } catch (error) {
    console.error('KYC cola error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}