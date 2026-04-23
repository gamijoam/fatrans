import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function POST(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token');

    const backendResponse = await fetch(`${BACKEND_URL}/api/v1/auth/logout-web`, {
      method: 'POST',
      headers: accessToken ? { 'Authorization': `Bearer ${accessToken.value}` } : {},
      credentials: 'include',
    });

    const setCookieHeaders = backendResponse.headers.getSetCookie();

    const response = NextResponse.json(
      { success: true, message: 'Sesión cerrada correctamente' },
      { status: 200 }
    );

    setCookieHeaders.forEach((cookie) => {
      response.headers.append('Set-Cookie', cookie);
    });

    return response;

  } catch (error) {
    console.error('Logout error:', error);
    return NextResponse.json(
      { message: 'Error al cerrar sesión' },
      { status: 500 }
    );
  }
}
