import { NextRequest, NextResponse } from 'next/server';
import { validateAdminAccess, validarUUID } from '@/lib/auth/admin-validation';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function GET(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    const authResult = validateAdminAccess({ accessToken });
    if (!authResult.valid) {
      return NextResponse.json({ message: authResult.message }, { status: authResult.status });
    }

    const { id } = params;
    if (!validarUUID(id)) {
      return NextResponse.json({ message: 'ID de verificación inválido' }, { status: 400 });
    }

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/kyc/revision/${id}`,
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
        { message: errorData.message || 'Error al obtener detalle' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });

  } catch (error) {
    console.error('KYC revision detail error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}