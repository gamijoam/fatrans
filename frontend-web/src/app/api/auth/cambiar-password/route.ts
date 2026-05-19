import { NextRequest, NextResponse } from 'next/server';
import { z } from 'zod';
import { enforceOriginPolicy } from '@/lib/security/origin-check';

// `export const dynamic = 'force-dynamic'` desactiva el cache automático de
// fetch() del lado del server en Next.js 14. Sin esto, los GET internos del BFF
// al backend Java se cachean por URL+headers, devolviendo respuestas viejas tras
// mutaciones (caso real Ronni QA 19-may-2026: KYC aprobado en BD pero el admin
// veía la solicitud aún en la cola).
export const dynamic = 'force-dynamic';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

const changePasswordRequestSchema = z.object({
  passwordActual: z.string().min(1, 'Contraseña actual requerida'),
  nuevoPassword: z
    .string()
    .min(8, 'La nueva contraseña debe tener al menos 8 caracteres')
    .max(128, 'La contraseña no puede exceder 128 caracteres')
    .regex(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/,
      'Debe incluir mayúsculas, minúsculas, números y caracteres especiales'
    ),
});

export async function POST(request: NextRequest) {
  const blocked = enforceOriginPolicy(request);
  if (blocked) return blocked;

  try {
    const body = await request.json();

    const result = changePasswordRequestSchema.safeParse(body);
    if (!result.success) {
      return NextResponse.json(
        { message: 'Datos inválidos', errors: result.error.flatten().fieldErrors },
        { status: 400 }
      );
    }

    const cookies = request.headers.get('cookie');
    
    const backendResponse = await fetch(`${BACKEND_URL}/api/v1/auth/cambiar-password`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Cookie': cookies || '',
      },
      credentials: 'include',
      body: JSON.stringify({
        passwordActual: body.passwordActual,
        nuevoPassword: body.nuevoPassword,
      }),
    });

    if (!backendResponse.ok) {
      const contentType = backendResponse.headers.get('content-type');
      console.error('Backend error:', backendResponse.status, contentType);
      
      let errorMessage = 'Error al cambiar contraseña';
      
      if (contentType && contentType.includes('application/json')) {
        try {
          const errorData = await backendResponse.json();
          errorMessage = errorData.message || errorMessage;
        } catch {
          errorMessage = `Error del servidor (${backendResponse.status})`;
        }
      } else {
        errorMessage = `Error del servidor (${backendResponse.status})`;
      }
      
      return NextResponse.json(
        { message: errorMessage },
        { status: backendResponse.status }
      );
    }

    return NextResponse.json(
      { message: 'Contraseña actualizada exitosamente' },
      { status: 200 }
    );

  } catch (error) {
    console.error('Cambiar password error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}
