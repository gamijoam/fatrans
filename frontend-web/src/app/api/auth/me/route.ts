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

    return NextResponse.json(userData, { status: 200 });

  } catch (error) {
    console.error('Get user error:', error);
    return NextResponse.json(
      { message: 'Error al obtener usuario' },
      { status: 500 }
    );
  }
}
