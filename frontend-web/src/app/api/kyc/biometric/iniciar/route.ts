import { NextRequest, NextResponse } from 'next/server';

// `export const dynamic = 'force-dynamic'` desactiva el cache automático de
// fetch() del lado del server en Next.js 14. Sin esto, los GET internos del BFF
// al backend Java se cachean por URL+headers, devolviendo respuestas viejas tras
// mutaciones (caso real Ronni QA 19-may-2026: KYC aprobado en BD pero el admin
// veía la solicitud aún en la cola).
export const dynamic = 'force-dynamic';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * BFF: inicia una sesión biométrica con el proveedor (Didit) y devuelve la URL
 * del widget para que el frontend la abra (iframe o nueva pestaña).
 */
export async function POST(request: NextRequest) {
  const token = request.cookies.get('access_token')?.value;
  if (!token) {
    return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
  }

  const backendResponse = await fetch(`${BACKEND_URL}/api/v1/kyc/biometric/iniciar`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token.replace(/^"|"$/g, '')}`,
      'X-Forwarded-For': request.headers.get('x-forwarded-for') || '',
      'User-Agent': request.headers.get('user-agent') || '',
    },
  });

  if (backendResponse.status >= 500) {
    return NextResponse.json(
      { message: 'Servicio temporalmente no disponible' },
      { status: 502 },
    );
  }

  if (!backendResponse.ok) {
    const errorData = await backendResponse.json().catch(() => ({}));
    return NextResponse.json(
      { message: errorData.message || 'Error iniciando verificación biométrica' },
      { status: backendResponse.status },
    );
  }

  const data = await backendResponse.json();
  return NextResponse.json(data, { status: 200 });
}
