import { NextRequest, NextResponse } from 'next/server';

// `export const dynamic = 'force-dynamic'` desactiva el cache automático de
// fetch() del lado del server en Next.js 14. Sin esto, los GET internos del BFF
// al backend Java se cachean por URL+headers, devolviendo respuestas viejas tras
// mutaciones (caso real Ronni QA 19-may-2026: KYC aprobado en BD pero el admin
// veía la solicitud aún en la cola).
export const dynamic = 'force-dynamic';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * BFF: registra el consentimiento biométrico (LOPDP) del socio autenticado.
 * Reenvía la cookie de auth y devuelve 201 en éxito.
 */
export async function POST(request: NextRequest) {
  const token = request.cookies.get('access_token')?.value;
  if (!token) {
    return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
  }

  const body = await request.json();

  const backendResponse = await fetch(`${BACKEND_URL}/api/v1/kyc/biometric/consentimiento`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token.replace(/^"|"$/g, '')}`,
      'X-Forwarded-For': request.headers.get('x-forwarded-for') || '',
      'User-Agent': request.headers.get('user-agent') || '',
    },
    body: JSON.stringify(body),
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
      { message: errorData.message || 'Error registrando consentimiento' },
      { status: backendResponse.status },
    );
  }

  return NextResponse.json({ success: true }, { status: 201 });
}
