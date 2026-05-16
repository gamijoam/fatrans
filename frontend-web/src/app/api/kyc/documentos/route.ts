import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * BFF: sube un documento KYC al backend Java.
 *
 * Nota: undici (HTTP client de Node fetch) agrega automáticamente
 * `;charset=UTF-8` al Content-Type cuando el body es FormData. El backend
 * tiene un filter (`MultipartContentTypeNormalizerFilter`) que sanea ese
 * Content-Type antes del MultipartResolver, así que acá solo armamos un
 * FormData estándar y dejamos que undici haga lo suyo.
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

    const backendFormData = new FormData();
    backendFormData.append('tipoDocumento', tipoDocumento);
    backendFormData.append('archivo', archivo);

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/kyc/documentos`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
        body: backendFormData,
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