import { NextRequest, NextResponse } from 'next/server';

// `export const dynamic = 'force-dynamic'` desactiva el cache automático de
// fetch() del lado del server en Next.js 14. Sin esto, los GET internos del BFF
// al backend Java se cachean por URL+headers, devolviendo respuestas viejas tras
// mutaciones (caso real Ronni QA 19-may-2026: KYC aprobado en BD pero el admin
// veía la solicitud aún en la cola).
export const dynamic = 'force-dynamic';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * BFF: el socio envía sus documentos a revisión.
 *
 * El backend exige `verificacionId` en el body (`EnviarDocumentosRequest`).
 * Antes este handler enviaba `JSON.stringify({})` y el backend respondía
 * 400 "verificacionId es requerido" — el frontend mostraba un toast genérico
 * porque el backend devuelve la causa en el campo `mensaje` (español, vía
 * `KYCExceptionHandler`) y el handler solo leía `errorData.message`.
 */
export async function POST(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    // Re-leemos el body que envió el frontend para forwardear `verificacionId`
    // al backend. Si el client no mandó body, asumimos vacío y dejamos que
    // el backend devuelva el 400 con el mensaje correcto.
    const body = await request.json().catch(() => ({}));

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/kyc/enviar`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
      }
    );

    if (!backendResponse.ok) {
      const errorData: { message?: string; mensaje?: string } =
        await backendResponse.json().catch(() => ({}));
      // Backend usa `mensaje` (KYCException) o `message` (validación Jakarta).
      const msg = errorData.message || errorData.mensaje || 'Error al enviar documentos';
      return NextResponse.json(
        { message: msg },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });

  } catch (error) {
    console.error('KYC enviar documentos error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}
