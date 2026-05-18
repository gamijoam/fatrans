import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

/**
 * Routing por subdominio:
 *
 *   fatrans.com.ve / www / qa.*           → landing pública en /
 *   auth.*  / qa-auth.*                   → solo flows de auth (login, registro,
 *                                            recuperar-password). El `/` raíz
 *                                            redirige a /login.
 *   app.*   / admin.* / qa-app / qa-admin → dashboard protegido. Si alguien
 *                                            navega a /login o /registro aquí,
 *                                            lo mandamos al subdominio auth.
 *
 * Bug previo: el `/registro` y `/login` se servían desde cualquier subdominio
 * porque el matcher era solo path-based, no host-based. Y `auth.*` servía la
 * landing en su raíz en vez del login.
 */

const AUTH_HOSTS = new Set([
  'auth.fatrans.com.ve',
  'qa-auth.fatrans.com.ve',
]);

const APP_HOSTS = new Set([
  'app.fatrans.com.ve',
  'admin.fatrans.com.ve',
  'qa-app.fatrans.com.ve',
  'qa-admin.fatrans.com.ve',
]);

const QA_HOSTS = new Set([
  'qa-auth.fatrans.com.ve',
  'qa-app.fatrans.com.ve',
  'qa-admin.fatrans.com.ve',
  'qa.fatrans.com.ve',
]);

const AUTH_ONLY_ROUTES = new Set([
  '/login',
  '/registro',
  '/recuperar-password',
  '/reset-password',
]);

const PUBLIC_ROUTES = new Set([
  '/',
  '/login',
  '/auth/login',
  '/registro',
  '/recuperar-password',
  '/reset-password',
]);

function authOriginFor(host: string): string {
  return QA_HOSTS.has(host)
    ? 'https://qa-auth.fatrans.com.ve'
    : 'https://auth.fatrans.com.ve';
}

export function middleware(request: NextRequest) {
  const { pathname, search } = request.nextUrl;
  const host = (request.headers.get('host') || '').toLowerCase();

  // 1. En el subdominio de auth, la raíz no muestra landing: va directo al login.
  if (AUTH_HOSTS.has(host) && pathname === '/') {
    return NextResponse.redirect(new URL('/login', request.url));
  }

  // 2. En app/admin, las rutas de auth se redirigen al subdominio correcto.
  //    Evita servir el login desde app.* (split-brain de UX y de cookies).
  if (APP_HOSTS.has(host) && AUTH_ONLY_ROUTES.has(pathname)) {
    return NextResponse.redirect(`${authOriginFor(host)}${pathname}${search}`);
  }

  // 3. Resto: protección por cookie.
  const isPublicRoute = PUBLIC_ROUTES.has(pathname);
  const isAuthApi = pathname.startsWith('/api/auth/');

  if (isPublicRoute || isAuthApi) return NextResponse.next();

  const accessToken = request.cookies.get('access_token');
  if (!accessToken) {
    const loginUrl = new URL('/login', request.url);
    loginUrl.searchParams.set('redirect', pathname);
    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)'],
};
