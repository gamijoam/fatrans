import { NextRequest, NextResponse } from 'next/server';
import { validateAdminAccess } from '@/lib/auth/admin-validation';
import { enforceOriginPolicy } from '@/lib/security/origin-check';

// `export const dynamic = 'force-dynamic'` desactiva el cache automático de
// fetch() del lado del server en Next.js 14. Sin esto, los GET internos del BFF
// al backend Java se cachean por URL+headers, devolviendo respuestas viejas tras
// mutaciones (caso real Ronni QA 19-may-2026: KYC aprobado en BD pero el admin
// veía la solicitud aún en la cola).
export const dynamic = 'force-dynamic';

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
