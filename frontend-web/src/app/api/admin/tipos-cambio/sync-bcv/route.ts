import { NextRequest, NextResponse } from 'next/server';
import { validateAdminAccess } from '@/lib/auth/admin-validation';

// Sin cache: este endpoint dispara un side effect (scrape al BCV + insert en BD).
// Si Next.js cacheara la respuesta, el botón parecería "no responder" en clicks
// sucesivos cuando el admin quiere forzar otro refresh.
export const dynamic = 'force-dynamic';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * BFF: dispara una sincronización manual del scraper BCV.
 *
 * El backend ya tiene un cron que corre cada 2h en horario laboral del BCV, pero
 * a veces el admin necesita forzar el scrape (caso real 20-may-2026: el deploy
 * del backend ocurrió DESPUÉS de la última ejecución del cron del día y la tasa
 * quedó stale hasta el día siguiente). Este botón cubre ese gap.
 */
export async function POST(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    const authResult = validateAdminAccess({ accessToken });
    if (!authResult.valid) {
      return NextResponse.json(
        { message: authResult.message },
        { status: authResult.status },
      );
    }

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/admin/tipos-cambio/sync-bcv`,
      {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        cache: 'no-store',
      },
    );

    const data = await backendResponse.json().catch(() => ({}));

    // 502 Bad Gateway del backend → el BCV no respondió o el HTML cambió.
    // Pasamos el mensaje literal para que el admin sepa qué falló.
    if (!backendResponse.ok) {
      return NextResponse.json(data, { status: backendResponse.status });
    }

    return NextResponse.json(data, { status: 200 });
  } catch (error) {
    console.error('Admin sync BCV error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 },
    );
  }
}
