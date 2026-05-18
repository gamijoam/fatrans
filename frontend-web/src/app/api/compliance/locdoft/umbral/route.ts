import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * BFF read-only para consultar el umbral LOCDOFT vigente (#218 PR-C).
 *
 * GET /api/compliance/locdoft/umbral?moneda=VES|USD
 * → { moneda: "VES", umbral: 10000.00 | null }
 */
export async function GET(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const moneda = (searchParams.get('moneda') || 'VES').toUpperCase();
    if (!['VES', 'USD'].includes(moneda)) {
      return NextResponse.json(
        { message: 'moneda inválida (se acepta VES o USD)' },
        { status: 400 },
      );
    }

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/compliance/locdoft/umbral?moneda=${moneda}`,
      {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
      },
    );

    if (!backendResponse.ok) {
      // fail-open: si el backend devuelve error, el frontend trata como
      // "sin umbral" y deja que el backend decida en el POST de operación.
      return NextResponse.json({ moneda, umbral: null }, { status: 200 });
    }

    const data = await backendResponse.json();
    return NextResponse.json(data, { status: 200 });
  } catch (error) {
    console.error('Locdoft umbral error:', error);
    return NextResponse.json({ moneda: 'VES', umbral: null }, { status: 200 });
  }
}
