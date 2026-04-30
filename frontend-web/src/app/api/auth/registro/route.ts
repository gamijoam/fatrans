import { NextRequest, NextResponse } from 'next/server';
import { registroSchema } from '@/lib/utils/validators';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function POST(request: NextRequest) {
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
