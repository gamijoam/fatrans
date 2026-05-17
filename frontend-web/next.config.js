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
