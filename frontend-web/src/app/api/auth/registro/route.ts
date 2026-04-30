import { NextRequest, NextResponse } from 'next/server';
import { registroSchema } from '@/lib/utils/validators';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function POST(request: NextRequest) {
  const origin = request.headers.get('origin');
  const referer = request.headers.get('referer');
  const allowedOrigins = [
    'http://localhost:3000',
    'http://localhost:13000',
    process.env.NEXT_PUBLIC_APP_URL, process.env.NEXT_PUBLIC_ADMIN_URL, process.env.NEXT_PUBLIC_AUTH_URL,
    process.env.NEXT_PUBLIC_APP_URL, process.env.NEXT_PUBLIC_APP_URL, 
  ].filter(Boolean);

  const allowedReferers = [
    'http://localhost:3000',
    'http://localhost:13000',
  ];

  if (origin && !allowedOrigins.includes(origin)) {
    return NextResponse.json({ message: 'Origen no permitido' }, { status: 403 });
  }

  if (referer && !allowedReferers.some((r) => referer.startsWith(r))) {
    return NextResponse.json({ message: 'Referer no permitido' }, { status: 403 });
  }

  try {
    const body = await request.json();

    const result = registroSchema.safeParse(body);
    if (!result.success) {
      return NextResponse.json(
        { message: 'Datos inválidos', errors: result.error.flatten().fieldErrors },
        { status: 400 }
      );
    }

    const backendResponse = await fetch(`${BACKEND_URL}/api/v1/socios/solicitud`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombreCompleto: body.nombreCompleto,
        cedula: body.cedula,
        correoElectronico: body.correoElectronico,
        telefono: body.telefono,
        empresa: body.empresa,
      }),
    });

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Error al procesar solicitud' },
        { status: backendResponse.status }
      );
    }

    return NextResponse.json(
      {
        success: true,
        message: 'Solicitud de registro enviada correctamente. Recibirás un correo cuando un administrador la apruebe.'
      },
      { status: 201 }
    );

  } catch (error) {
    console.error('Registro error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}
