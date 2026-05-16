import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * BFF: sube un documento KYC al backend Java.
 *
 * IMPORTANTE — bug de Spring + undici:
 * Cuando usamos `body: FormData` en Node fetch, undici construye un
 * Content-Type como `multipart/form-data;boundary=...;charset=UTF-8`. Spring
 * Boot 3 rechaza ese Content-Type porque el `charset=UTF-8` no está
 * contemplado en su `StandardMultipartHttpServletRequest` — devuelve un 500
 * con "Content-Type ... is not supported". El RFC 7578 permite el charset
 * en teoría, pero Spring lo trata como inválido.
 *
 * Solución: armamos el cuerpo multipart manualmente con un boundary
 * controlado y mandamos el Content-Type sin charset. Así Spring lo parsea
 * correctamente sin tocar la config del backend.
 */
export async function POST(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    const formData = await request.formData();
    const tipoDocumento = formData.get('tipoDocumento') as string;
    const archivo = formData.get('archivo') as File;

    if (!tipoDocumento || !archivo) {
      return NextResponse.json(
        { message: 'Faltan campos requeridos' },
        { status: 400 }
      );
    }

    // Boundary único — usamos Math.random porque crypto.randomBytes requiere
    // import async en edge runtime; el boundary no necesita ser secreto, solo
    // único dentro del request.
    const boundary = `----fatrans-${Math.random().toString(36).slice(2)}${Date.now()}`;
    const archivoBuffer = Buffer.from(await archivo.arrayBuffer());

    const preamble = Buffer.from(
      `--${boundary}\r\n` +
      `Content-Disposition: form-data; name="tipoDocumento"\r\n\r\n` +
      `${tipoDocumento}\r\n` +
      `--${boundary}\r\n` +
      `Content-Disposition: form-data; name="archivo"; filename="${archivo.name || 'archivo'}"\r\n` +
      `Content-Type: ${archivo.type || 'application/octet-stream'}\r\n\r\n`,
      'utf-8'
    );
    const closing = Buffer.from(`\r\n--${boundary}--\r\n`, 'utf-8');
    const body = Buffer.concat([preamble, archivoBuffer, closing]);

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/kyc/documentos`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          // Sin charset — Spring lo necesita así.
          'Content-Type': `multipart/form-data; boundary=${boundary}`,
          'Content-Length': body.length.toString(),
        },
        body: body as unknown as BodyInit,
      }
    );

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Error al subir documento' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 201 });

  } catch (error) {
    console.error('KYC subir documento error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}