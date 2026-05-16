import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * BFF: sube un documento KYC al backend Java.
 *
 * El backend NO acepta multipart — recibe `SubirDocumentoRequest` JSON con
 * el archivo en base64 (campo `archivoBase64`). El frontend nos manda el
 * archivo como multipart porque es ergonómico para el `<input type="file">`,
 * así que acá hacemos la conversión: leemos el File, lo codificamos a base64,
 * y armamos el JSON con todos los campos que el backend valida (verificacionId,
 * tipoDocumento, archivoBase64, nombreOriginal, tamanoBytes, mimeType).
 */
export async function POST(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    const formData = await request.formData();
    const tipoDocumento = formData.get('tipoDocumento') as string;
    const verificacionId = formData.get('verificacionId') as string;
    const archivo = formData.get('archivo') as File;

    if (!tipoDocumento || !archivo || !verificacionId) {
      return NextResponse.json(
        { message: 'Faltan campos: verificacionId, tipoDocumento y archivo son requeridos' },
        { status: 400 }
      );
    }

    // Convertimos el archivo a base64. Node Buffer.toString('base64') es la
    // forma estándar — no usamos atob/btoa (esos rompen con binarios > UTF-8).
    const arrayBuffer = await archivo.arrayBuffer();
    const base64 = Buffer.from(arrayBuffer).toString('base64');

    const payload = {
      verificacionId,
      tipoDocumento,
      archivoBase64: base64,
      nombreOriginal: archivo.name || 'archivo',
      tamanoBytes: archivo.size,
      mimeType: archivo.type || 'application/octet-stream',
    };

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/kyc/documentos`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      }
    );

    if (!backendResponse.ok) {
      const errorData: { message?: string; mensaje?: string } =
        await backendResponse.json().catch(() => ({}));
      // Backend usa `mensaje` (KYCException) o `message` (validación Jakarta).
      // Probamos ambos para que el toast muestre la causa real, no un genérico.
      const msg = errorData.message || errorData.mensaje || 'Error al subir documento';
      return NextResponse.json(
        { message: msg },
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
