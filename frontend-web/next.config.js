/** @type {import('next').NextConfig} */
const { execSync } = require('child_process');

const nextConfig = {
  generateBuildId: async () => {
    try {
      return execSync('git rev-parse --short HEAD').toString().trim();
    } catch {
      return 'fatrans-build-prod';
    }
  },
  output: 'standalone',
  reactStrictMode: true,
  swcMinify: true,
  compiler: {
    removeConsole: process.env.NODE_ENV === 'production',
  },
  images: {
    domains: ['localhost', 'minio', 'fatrans.com.ve'],
    formats: ['image/webp', 'image/avif'],
    // Permite SVG locales (logo institucional Fatrans). El CSP restrictivo
    // evita ejecución de scripts embebidos en SVGs maliciosos. Solo se
    // aceptan SVGs servidos desde nuestro propio /public — los uploads de
    // usuario siguen rechazándose en otros endpoints (documentospdf, KYC).
    dangerouslyAllowSVG: true,
    contentDispositionType: 'attachment',
    contentSecurityPolicy: "default-src 'self'; script-src 'none'; sandbox;",
  },
  async headers() {
    return [
      {
        source: '/:path*',
        headers: [
          { key: 'X-Content-Type-Options', value: 'nosniff' },
          { key: 'X-Frame-Options', value: 'DENY' },
          { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' },
          { key: 'Permissions-Policy', value: 'camera=(), microphone=(), geolocation=(), payment=()' },
        ],
      },
      {
        // No-cache global para TODAS las respuestas de BFF (`/api/*`).
        //
        // Motivo (mayo-2026): bugs en cascada porque el navegador estaba
        // cacheando GET /api/auth/me, GET /api/kyc/estado, etc. Después de
        // mutar estado en el backend, el frontend pedía el estado nuevo y
        // recibía la versión vieja desde caché del navegador o de un
        // intermediario. Resultados visibles:
        //   - Modal de cambio de contraseña re-aparecía (#301)
        //   - Verificación biométrica no avanzaba al paso 2 (#302)
        //   - Botón "Enviar a revisión" no aparecía tras subir comprobante
        //     porque /kyc/estado servía la respuesta sin el documento (#303)
        //
        // Aplicar el header acá cubre los 76 endpoints BFF de una vez en
        // lugar de tener que recordar agregar `noCacheHeaders()` en cada
        // route nuevo (riesgoso a largo plazo). Las páginas Next.js NO se
        // ven afectadas porque este `source` solo matchea /api/*.
        //
        // Si en el futuro algún endpoint BFF necesita ser cacheable (e.g.
        // datos públicos que cambian rara vez), puede sobreescribir este
        // header desde su `route.ts` con `NextResponse.json(data, {
        // headers: { 'Cache-Control': 'public, max-age=...' } })`.
        source: '/api/:path*',
        headers: [
          {
            key: 'Cache-Control',
            value: 'no-store, no-cache, must-revalidate, proxy-revalidate',
          },
          { key: 'Pragma', value: 'no-cache' },
          { key: 'Expires', value: '0' },
        ],
      },
      {
        source: '/app/:path*',
        headers: [{ key: 'X-Robots-Tag', value: 'noindex, nofollow' }],
      },
      {
        source: '/admin/:path*',
        headers: [{ key: 'X-Robots-Tag', value: 'noindex, nofollow' }],
      },
    ];
  },
};

module.exports = nextConfig;
