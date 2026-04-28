import { NextRequest, NextResponse } from 'next/server';
import { validateAdminAccess, validarUUID } from '@/lib/auth/admin-validation';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function POST(
  request: NextRequest,
  { params }: { params: { id: string; action: string } }
) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    const authResult = validateAdminAccess({ accessToken });
    if (!authResult.valid) {
      return NextResponse.json({ message: authResult.message }, { status: authResult.status });
    }

    const { id, action } = params;
    if (!validarUUID(id)) {
      return NextResponse.json({ message: 'ID de verificación inválido' }, { status: 400 });
    }

    if (action !== 'aprobar' && action !== 'rechazar') {
      return NextResponse.json({ message: 'Acción no válida' }, { status: 400 });
    }

    const body = await request.json().catch(() => ({}));

    const endpoint = action === 'aprobar'
      ? `${BACKEND_URL}/api/v1/kyc/revision/${id}/aprobar`
      : `${BACKEND_URL}/api/v1/kyc/revision/${id}/rechazar`;

    const backendResponse = await fetch(endpoint, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(body),
    });

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || `Error al ${action}` },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });

  } catch (error) {
    console.error('KYC revision action error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}