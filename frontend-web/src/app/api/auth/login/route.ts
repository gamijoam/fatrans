import { NextRequest, NextResponse } from 'next/server';
import { loginSchema } from '@/lib/utils/validators';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function POST(request: NextRequest) {
  const origin = request.headers.get('origin');
  const allowedOrigins = [
    'http://localhost:3000',
    'http://localhost:13000',
    process.env.NEXT_PUBLIC_APP_URL,
    process.env.NEXT_PUBLIC_ADMIN_URL,
    process.env.NEXT_PUBLIC_AUTH_URL,
    process.env.NEXT_PUBLIC_APP_URL,
    
  ].filter(Boolean);

  if (origin && !allowedOrigins.includes(origin)) {
    return NextResponse.json({ message: 'Origen no permitido' }, { status: 403 });
  }

  try {
    const body = await request.json();

    const result = loginSchema.safeParse(body);
    if (!result.success) {
      return NextResponse.json(
        { message: 'Datos inválidos', errors: result.error.flatten().fieldErrors },
        { status: 400 }
      );
    }

    const backendResponse = await fetch(`${BACKEND_URL}/api/v1/auth/login-web`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        identificador: body.identificador,
        password: body.password
      }),
      credentials: 'include',
    });

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Credenciales inválidas' },
        { status: backendResponse.status }
      );
    }

    const setCookieHeaders = backendResponse.headers.getSetCookie();
    
    let accessToken = setCookieHeaders
      .find(c => c.startsWith('access_token='))
      ?.split(';')[0]?.replace('access_token=', '') || '';
      
    accessToken = accessToken.replace(/^"|"$/g, '');
    
    const userId = backendResponse.headers.get('X-User-Id') || '';
    const userRol = backendResponse.headers.get('X-User-Rol') || '';
    
    interface UserData {
      id: string;
      nombreUsuario?: string;
      correoElectronico?: string;
      nombreCompleto?: string;
      rol: string;
      socioId?: string;
      debeCambiarPassword: boolean;
    }
    
    let userData: UserData = {
      id: userId,
      rol: userRol,
      debeCambiarPassword: false,
    };
    
    if (accessToken) {
      try {
        const meResponse = await fetch(`${BACKEND_URL}/api/v1/auth/me`, {
          headers: { 'Authorization': `Bearer ${accessToken}` },
          credentials: 'include',
        });
        
        console.log('/me response status:', meResponse.status);
        console.log('/me content-type:', meResponse.headers.get('content-type'));
        
        if (meResponse.ok) {
          const contentType = meResponse.headers.get('content-type');
          if (contentType && contentType.includes('application/json')) {
            const meData = await meResponse.json();
            console.log('/me data:', meData);
            userData = {
              id: meData.id,
              nombreUsuario: meData.nombreUsuario,
              correoElectronico: meData.correoElectronico,
              nombreCompleto: meData.nombreCompleto,
              rol: meData.rol,
              socioId: meData.socioId,
              debeCambiarPassword: meData.debeCambiarPassword ?? false,
            };
          }
        } else {
          console.log('/me failed, status:', meResponse.status);
        }
      } catch (e) {
        console.error('Error fetching /me:', e);
      }
    }

    const response = NextResponse.json(
      { success: true, user: userData },
      { status: 200 }
    );

    setCookieHeaders.forEach((cookie) => {
      const cleanCookie = cookie.replace('; Secure', '');
      response.headers.append('Set-Cookie', cleanCookie);
    });

    return response;

  } catch (error) {
    console.error('Login error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}
