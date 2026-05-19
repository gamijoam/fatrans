import { NextRequest, NextResponse } from 'next/server';

// `export const dynamic = 'force-dynamic'` desactiva el cache automático de
// fetch() del lado del server en Next.js 14. Sin esto, los GET internos del BFF
// al backend Java se cachean por URL+headers, devolviendo respuestas viejas tras
// mutaciones (caso real Ronni QA 19-may-2026: KYC aprobado en BD pero el admin
// veía la solicitud aún en la cola).
export const dynamic = 'force-dynamic';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * BFF: descarga un documento KYC para el admin/analista.
 *
 * El backend almacena los archivos en MinIO interno (`fatrans-minio:9000`),
 * que no es accesible desde el browser. Antes el backend devolvía pre-signed
 * URLs con ese hostname → 404 al abrir. Ahora el backend expone
 * `GET /api/v1/kyc/admin/documentos/{id}/descargar` que stream el archivo,
 * y este BFF proxea esa request agregando el JWT del admin.
 */
export async function GET(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  const accessToken = request.cookies.get('access_token')?.value;
  if (!accessToken) {
    return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
  }

  const backendResponse = await fetch(
    `${BACKEND_URL}/api/v1/kyc/admin/documentos/${params.id}/descargar`,
    {
      method: 'GET',
      headers: { Authorization: `Bearer ${accessToken}` },
    }
  );

  if (!backendResponse.ok) {
    const errorData: { message?: string; mensaje?: string } =
      await backendResponse.json().catch(() => ({}));
    return NextResponse.json(
      { message: errorData.message || errorData.mensaje || 'Error al descargar documento' },
      { status: backendResponse.status }
    );
  }

  // Forwardear el blob + headers de Content-Type / Content-Disposition para
  // que el browser pueda renderizar inline (PDF nativo) o descargarlo.
  const blob = await backendResponse.blob();
  const headers = new Headers();
  const contentType = backendResponse.headers.get('Content-Type');
  if (contentType) headers.set('Content-Type', contentType);
  const contentDisposition = backendResponse.headers.get('Content-Disposition');
  if (contentDisposition) headers.set('Content-Disposition', contentDisposition);

  return new NextResponse(blob, { status: 200, headers });
}
