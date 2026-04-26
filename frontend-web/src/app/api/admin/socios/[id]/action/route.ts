import { NextRequest, NextResponse } from 'next/server';
import { validateAdminAccess } from '@/lib/auth/admin-validation';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function POST(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    const authResult = validateAdminAccess({ accessToken });
    if (!authResult.valid) {
      return NextResponse.json({ message: authResult.message }, { status: authResult.status });
    }

    const body = await request.json().catch(() => ({}));
    const action = body.action;

    let endpoint = '';
    if (action === 'activar') {
      endpoint = `${BACKEND_URL}/api/v1/socios/${params.id}/activar`;
    } else if (action === 'desactivar') {
      endpoint = `${BACKEND_URL}/api/v1/socios/${params.id}/desactivar?motivo=${encodeURIComponent(body.motivo || 'Desactivado por administrador')}`;
    } else {
      return NextResponse.json(
        { message: 'Acción inválida. Use "activar" o "desactivar"' },
        { status: 400 }
      );
    }

    const backendResponse = await fetch(endpoint, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Error al actualizar socio' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });

  } catch (error) {
    console.error('Admin socio action error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}