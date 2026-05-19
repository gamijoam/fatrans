import { NextRequest, NextResponse } from 'next/server';

// `export const dynamic = 'force-dynamic'` desactiva el cache automático de
// fetch() del lado del server en Next.js 14. Sin esto, los GET internos del BFF
// al backend Java se cachean por URL+headers, devolviendo respuestas viejas tras
// mutaciones (caso real Ronni QA 19-may-2026: KYC aprobado en BD pero el admin
// veía la solicitud aún en la cola).
export const dynamic = 'force-dynamic';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * BFF para descargar el comprobante PDF de un movimiento (issue #220 PR-B).
 *
 * A diferencia de otros endpoints de documentos que devuelven JSON con una
 * presigned URL, este pasa los bytes del PDF tal cual desde el backend al
 * cliente. El backend no persiste el comprobante — lo regenera on-demand
 * desde el movimiento (RN-006).
 */
export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ numeroCuenta: string; numeroOperacion: string }> }
) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    const { numeroCuenta, numeroOperacion } = await params;

    const backendResponse = await fetch(
      `${BACKEND_URL}/api/v1/cuentas/${encodeURIComponent(numeroCuenta)}/movimientos/${encodeURIComponent(numeroOperacion)}/comprobante`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
        // No 'Content-Type' aquí — es una GET sin body
      }
    );

    if (!backendResponse.ok) {
      // El backend devuelve JSON en caso de error (gracias al exception handler)
      const errorBody = await backendResponse.text();
      let errorMessage = `Error al descargar comprobante (${backendResponse.status})`;
      try {
        const parsed = JSON.parse(errorBody);
        errorMessage = parsed.message || parsed.error || errorMessage;
      } catch {
        // Body no es JSON — usar el text crudo si no está vacío
        if (errorBody) errorMessage = errorBody.substring(0, 200);
      }
      return NextResponse.json(
        { message: errorMessage },
        { status: backendResponse.status }
      );
    }

    // Pasar bytes del PDF al cliente
    const pdfBuffer = await backendResponse.arrayBuffer();
    const filename = `Comprobante_${numeroOperacion}.pdf`;

    return new NextResponse(pdfBuffer, {
      status: 200,
      headers: {
        'Content-Type': 'application/pdf',
        'Content-Disposition': `attachment; filename="${filename}"`,
        'Cache-Control': 'no-store, no-cache, must-revalidate, private',
      },
    });

  } catch (error) {
    console.error('Comprobante movimiento error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}
