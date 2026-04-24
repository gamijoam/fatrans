import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

const ADMIN_ROLES = ['ADMIN', 'ADMINISTRADOR', 'GESTOR', 'SUPER_ADMIN'];

function validateAdminRole(token: string): boolean {
  try {
    const payload = token.split('.')[1];
    const decoded = Buffer.from(payload, 'base64').toString('utf-8');
    const data = JSON.parse(decoded);
    return ADMIN_ROLES.includes(data.rol);
  } catch {
    return false;
  }
}

export async function GET(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token');
    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    if (!validateAdminRole(accessToken.value)) {
      return NextResponse.json({ message: 'No autorizado' }, { status: 403 });
    }

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/admin/dashboard/estadisticas`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${accessToken.value}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      }
    );

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Error al obtener estadísticas' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });

  } catch (error) {
    console.error('Dashboard stats error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}
