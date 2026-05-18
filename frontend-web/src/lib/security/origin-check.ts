import { NextResponse } from 'next/server';

/**
 * Whitelist de Origins permitidos para las API routes del BFF (Next.js).
 *
 * Por qué un módulo compartido en vez de duplicar `allowedOrigins` en cada
 * route handler:
 *
 *  1. **Inlining de NEXT_PUBLIC_* al BUILD**: Next.js sustituye
 *     `process.env.NEXT_PUBLIC_X` por la literal en build-time, no en
 *     runtime. Si el build no recibió un `--build-arg`, el bundle queda
 *     con el default del Dockerfile o con `undefined`, y el `process.env`
 *     del runtime ya no lo alcanza. Eso causó el bug del 18-may-2026:
 *     `NEXT_PUBLIC_AUTH_URL` no se pasó como build-arg → quedó horneado
 *     con el default `https://fatrans.com.ve` en vez de
 *     `https://auth.fatrans.com.ve` → usuarios entrando por
 *     `www.fatrans.com.ve/registro` recibían 403 "Origen no permitido".
 *
 *  2. **www y root variantes**: los usuarios escriben `www.` o no,
 *     entran por subdominio público o protegido. Hay que aceptar TODOS
 *     los hosts canónicos donde puede vivir un form, no solo los que
 *     coincidan con un env var puntual.
 *
 *  3. **PROD + QA en mismo bundle no aplica** pero por defensa-en-
 *     profundidad y por DX (corre QA local apuntando a PROD si quisiera),
 *     incluimos ambos sets explícitos.
 *
 * El check sigue siendo opt-in: la API route llama a `enforceOriginPolicy`
 * al inicio del handler y, si devuelve un NextResponse, lo retorna tal cual.
 */

const STATIC_ALLOWED_ORIGINS = [
  // Local development
  'http://localhost:3000',
  'http://localhost:3001',
  'http://localhost:13000',

  // Producción — todos los hosts canónicos
  'https://fatrans.com.ve',
  'https://www.fatrans.com.ve',
  'https://app.fatrans.com.ve',
  'https://admin.fatrans.com.ve',
  'https://auth.fatrans.com.ve',
  'https://api.fatrans.com.ve',

  // QA — todos los hosts canónicos
  'https://qa.fatrans.com.ve',
  'https://www.qa.fatrans.com.ve',
  'https://qa-app.fatrans.com.ve',
  'https://qa-admin.fatrans.com.ve',
  'https://qa-auth.fatrans.com.ve',
  'https://qa-api.fatrans.com.ve',
] as const;

/**
 * Devuelve la lista efectiva de origins permitidos:
 * la estática + lo que indiquen los env vars (por si en QA/PROD
 * apunta a un dominio nuevo que aún no está en STATIC).
 */
export function getAllowedOrigins(): string[] {
  const envOrigins = [
    process.env.NEXT_PUBLIC_APP_URL,
    process.env.NEXT_PUBLIC_ADMIN_URL,
    process.env.NEXT_PUBLIC_AUTH_URL,
  ].filter((v): v is string => typeof v === 'string' && v.length > 0);

  return Array.from(new Set<string>([...STATIC_ALLOWED_ORIGINS, ...envOrigins]));
}

export function isOriginAllowed(origin: string): boolean {
  return getAllowedOrigins().includes(origin);
}

/**
 * Para Referer no hacemos startsWith (vulnerable a
 * `https://app.fatrans.com.ve.evil.com`); parseamos con `new URL().origin`
 * y comparamos contra la misma lista.
 */
export function isRefererAllowed(referer: string): boolean {
  try {
    return isOriginAllowed(new URL(referer).origin);
  } catch {
    return false;
  }
}

/**
 * Verifica Origin + Referer de la request. Devuelve un NextResponse listo
 * para retornar si la request está bloqueada, o `null` si pasa el check.
 *
 *     export async function POST(request: NextRequest) {
 *       const blocked = enforceOriginPolicy(request);
 *       if (blocked) return blocked;
 *       // ... rest of handler
 *     }
 */
export function enforceOriginPolicy(request: Request): NextResponse | null {
  const origin = request.headers.get('origin');
  const referer = request.headers.get('referer');

  if (origin && !isOriginAllowed(origin)) {
    return NextResponse.json({ message: 'Origen no permitido' }, { status: 403 });
  }
  if (referer && !isRefererAllowed(referer)) {
    return NextResponse.json({ message: 'Referer no permitido' }, { status: 403 });
  }
  return null;
}
