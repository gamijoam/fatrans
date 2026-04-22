import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  const pathname = request.nextUrl.pathname;
  const publicRoutes = ['/', '/login', '/registro', '/recuperar-password'];
  const isPublicRoute = publicRoutes.some((route) => pathname === route);

  if (isPublicRoute) return NextResponse.next();

  const accessToken = request.cookies.get('access_token');
  if (!accessToken) {
    const loginUrl = new URL('/login', request.url);
    loginUrl.searchParams.set('redirect', pathname);
    return NextResponse.redirect(loginUrl);
  }

  if (pathname.startsWith('/admin')) {
    const userCookie = request.cookies.get('usuario');
    if (userCookie) {
      try {
        const user = JSON.parse(userCookie.value);
        if (!['ADMIN', 'ADMINISTRADOR', 'GESTOR'].includes(user.rol)) {
          return NextResponse.redirect(new URL('/dashboard', request.url));
        }
      } catch {
        return NextResponse.redirect(new URL('/login', request.url));
      }
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)'],
};
