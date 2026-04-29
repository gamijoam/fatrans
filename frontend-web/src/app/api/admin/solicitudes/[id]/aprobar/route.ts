import { NextRequest, NextResponse } from 'next/server';
import { validateAdminAccess } from '@/lib/auth/admin-validation';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const origin = request.headers.get('origin');
  const allowedOrigins = [
    'http://localhost:3000',
    'http://localhost:13000',
    process.env.NEXT_PUBLIC_APP_URL, process.env.NEXT_PUBLIC_ADMIN_URL, 'https://auth.fatrans.com.ve', 'https://www.fatrans.com.ve', 'https://fatrans.com.ve',
  ].filter(Boolean);

  if (origin && !allowedOrigins.includes(origin)) {
    return NextResponse.json({ message: 'Origen no permitido' }, { status: 403 });
  }

  try {
    const accessToken = request.cookies.get('access_token');
    const authResult = validateAdminAccess({ accessToken: accessToken?.value });
    if (!authResult.valid) {
      return NextResponse.json({ message: authResult.message }, { status: authResult.status });
    }
    const token = accessToken!.value;

    const { id } = await params;
    const body = await request.json().catch(() => ({}));

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/socios/solicitudes/${id}/aprobar`,
      {
        method: 'POST',
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
        { message: errorData.message || 'Error al aprobar solicitud' },
        { status: backendResponse.status }
      );
    }

    return NextResponse.json(
      { success: true, message: 'Solicitud aprobada' },
      { status: 200 }
    );

  } catch (error) {
    console.error('Aprobar solicitud error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}