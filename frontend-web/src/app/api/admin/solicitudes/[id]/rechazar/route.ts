import { NextRequest, NextResponse } from 'next/server';
import { validateAdminAccess } from '@/lib/auth/admin-validation';
import { enforceOriginPolicy } from '@/lib/security/origin-check';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const blocked = enforceOriginPolicy(request);
  if (blocked) return blocked;

  try {
    const accessToken = request.cookies.get('access_token');
    const authResult = validateAdminAccess({ accessToken: accessToken?.value });
    if (!authResult.valid) {
      return NextResponse.json({ message: authResult.message }, { status: authResult.status });
    }
    const token = accessToken!.value;

    const { id } = await params;
    const body = await request.json();

    if (!body.motivo || body.motivo.trim().length === 0) {
      return NextResponse.json(
        { message: 'El motivo de rechazo es obligatorio' },
        { status: 400 }
      );
    }

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/socios/solicitudes/${id}/rechazar`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({ motivo: body.motivo }),
      }
    );

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Error al rechazar solicitud' },
        { status: backendResponse.status }
      );
    }

    return NextResponse.json(
      { success: true, message: 'Solicitud rechazada' },
      { status: 200 }
    );

  } catch (error) {
    console.error('Rechazar solicitud error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}