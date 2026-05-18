import { NextRequest, NextResponse } from 'next/server';
import { validateAdminAccess, validarNumeroSolicitud } from '@/lib/auth/admin-validation';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ numero: string }> }
) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    const authResult = validateAdminAccess({ accessToken });
    if (!authResult.valid) {
      return NextResponse.json({ message: authResult.message }, { status: authResult.status });
    }

    const { numero } = await params;
    if (!validarNumeroSolicitud(numero)) {
      return NextResponse.json({ message: 'Parámetro inválido' }, { status: 400 });
    }

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/creditos/solicitudes/${encodeURIComponent(numero)}/plan`,
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
        { message: errorData.message || 'Error al obtener plan de amortización' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });

  } catch (error) {
    console.error('Admin plan amortizacion error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}