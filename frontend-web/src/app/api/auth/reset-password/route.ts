import { NextRequest, NextResponse } from 'next/server';

// `export const dynamic = 'force-dynamic'` desactiva el cache automático de
// fetch() del lado del server en Next.js 14. Sin esto, los GET internos del BFF
// al backend Java se cachean por URL+headers, devolviendo respuestas viejas tras
// mutaciones (caso real Ronni QA 19-may-2026: KYC aprobado en BD pero el admin
// veía la solicitud aún en la cola).
export const dynamic = 'force-dynamic';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();

    if (!body.token || !body.nuevaPassword) {
      return NextResponse.json(
        { message: 'Token y nueva contraseña son requeridos' },
        { status: 400 }
      );
    }

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/auth/reset-password`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          token: body.token,
          nuevaPassword: body.nuevaPassword,
        }),
      }
    );

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Error al restablecer contraseña' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });

  } catch (error) {
    console.error('Reset password error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}
