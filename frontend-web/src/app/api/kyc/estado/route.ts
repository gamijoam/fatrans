import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

// Sin esto Next.js 14 cachea agresivamente la respuesta del GET. Síntoma:
// el socio completaba la verificación biométrica en Didit, el webhook
// llegaba y la BD pasaba a estadoBiometria=APROBADA, pero el dashboard
// seguía mostrando "Abrir verificación" porque el browser recibía la
// respuesta cacheada con estadoBiometria=NO_INICIADA.
export const dynamic = 'force-dynamic';
export const revalidate = 0;

export async function GET(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/kyc/estado`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        // Pasamos cache: 'no-store' además del export dinámico, así
        // garantizamos que Next.js no cacheé tampoco la llamada al
        // backend interno.
        cache: 'no-store',
      }
    );

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      // Backend retorna 500 + KYC_000 cuando el socio no tiene KYC iniciado
      if (errorData.error === 'KYC_000' || backendResponse.status === 500) {
        return NextResponse.json({ estado: 'SIN_KYC', mensaje: 'No has iniciado el proceso KYC' }, { status: 200 });
      }
      return NextResponse.json(
        { message: errorData.message || 'Error al obtener estado KYC' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });

  } catch (error) {
    console.error('KYC estado error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}