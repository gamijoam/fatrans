import { NextRequest, NextResponse } from 'next/server';
import { validateAdminAccess } from '@/lib/auth/admin-validation';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function GET(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    const authResult = validateAdminAccess({ accessToken });
    if (!authResult.valid) {
      return NextResponse.json({ message: authResult.message }, { status: authResult.status });
    }

    const { searchParams } = new URL(request.url);

    const params = new URLSearchParams();
    const nombre = searchParams.get('nombre');
    const numeroDocumento = searchParams.get('numeroDocumento');
    const numeroSocio = searchParams.get('numeroSocio');
    const correo = searchParams.get('correo');
    const estado = searchParams.get('estado');
    const page = searchParams.get('page') || '0';
    const size = searchParams.get('size') || '20';
    const sortBy = searchParams.get('sortBy') || 'fechaRegistro';
    const direction = searchParams.get('direction') || 'desc';

    if (nombre) params.set('nombre', nombre);
    if (numeroDocumento) params.set('numeroDocumento', numeroDocumento);
    if (numeroSocio) params.set('numeroSocio', numeroSocio);
    if (correo) params.set('correo', correo);
    params.set('page', page);
    params.set('size', size);
    params.set('sortBy', sortBy);
    params.set('direction', direction);

    const endpoint = nombre || numeroDocumento || numeroSocio || correo
      ? `${BACKEND_URL}/api/v1/socios/buscar?${params.toString()}`
      : `${BACKEND_URL}/api/v1/socios?${params.toString()}`;

    const backendResponse = await fetch(endpoint, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Error al obtener socios' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });

  } catch (error) {
    console.error('Admin socios list error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}