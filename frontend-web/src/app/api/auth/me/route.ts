import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

export async function GET(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token');

    if (!accessToken) {
      return NextResponse.json(
        { message: 'No autenticado' },
        { status: 401 }
      );
    }

    let accessTokenValue = accessToken.value;
    accessTokenValue = accessTokenValue.replace(/^"|"$/g, '');

    const backendResponse = await fetch(`${BACKEND_URL}/api/v1/auth/me`, {
      method: 'GET',
      headers: { 'Authorization': `Bearer ${accessTokenValue}` },
      credentials: 'include',
    });

    if (!backendResponse.ok) {
      return NextResponse.json(
        { message: 'Sesión inválida o expirada' },
        { status: backendResponse.status }
      );
    }

    const userData = await backendResponse.json();

    // No-cache headers: el cliente (incluido el modal de cambio de password)
    // necesita poder re-sincronizar con el backend inmediatamente después de
    // mutaciones de usuario (cambio de password, actualización de perfil, etc).
    // Sin esto, el navegador puede servir una versión vieja del usuario
    // (caso real Gabriel QA 19-may-2026: vio `debeCambiarPassword=true`
    // cacheado después de haberlo cambiado correctamente).
    return NextResponse.json(userData, {
      status: 200,
      headers: {
        'Cache-Control': 'no-store, no-cache, must-revalidate, proxy-revalidate',
        Pragma: 'no-cache',
        Expires: '0',
      },
    });

  } catch (error) {
    console.error('Get user error:', error);
    return NextResponse.json(
      { message: 'Error al obtener usuario' },
      { status: 500 }
    );
  }
}
