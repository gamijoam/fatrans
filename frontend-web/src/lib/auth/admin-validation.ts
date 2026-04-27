import jwt from 'jsonwebtoken';

const ADMIN_ROLES = ['ADMIN', 'ADMINISTRADOR', 'GESTOR', 'SUPER_ADMIN'];
const JWT_SECRET = process.env.JWT_SECRET || process.env.NEXTAUTH_SECRET || 'fallback-secret-do-not-use-in-production';

export interface AdminAuthContext {
  accessToken: string | undefined;
}

export interface TokenPayload {
  sub: string;
  rol: string;
  tipo: string;
  iat?: number;
  exp?: number;
}

export function validateAdminAccess(context: AdminAuthContext): { valid: boolean; status: number; message: string } {
  if (!context.accessToken) {
    return { valid: false, status: 401, message: 'No autenticado' };
  }

  try {
    const decoded = jwt.verify(context.accessToken, JWT_SECRET) as TokenPayload;

    if (!decoded.rol || !ADMIN_ROLES.includes(decoded.rol)) {
      return { valid: false, status: 403, message: 'No autorizado' };
    }

    return { valid: true, status: 200, message: 'OK' };
  } catch (error) {
    if (error instanceof jwt.TokenExpiredError) {
      return { valid: false, status: 401, message: 'Token expirado' };
    }
    if (error instanceof jwt.JsonWebTokenError) {
      return { valid: false, status: 401, message: 'Token inválido' };
    }
    return { valid: false, status: 401, message: 'Token inválido' };
  }
}

export function decodeToken(token: string): TokenPayload | null {
  try {
    return jwt.decode(token) as TokenPayload;
  } catch {
    return null;
  }
}

export function validarNumeroSolicitud(numero: string): boolean {
  return /^[A-Z0-9-]{5,30}$/.test(numero);
}

export function sanitizarTexto(texto: string): string {
  if (typeof texto !== 'string') return '';
  return texto.replace(/[<>\"']/g, '');
}

export function esNumeroValido(value: unknown): value is number {
  return typeof value === 'number' && !isNaN(value) && isFinite(value);
}

export function validarUUID(uuid: string): boolean {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(uuid);
}

export function validarAnio(anio: string): boolean {
  const year = parseInt(anio, 10);
  return !isNaN(year) && year >= 2020 && year <= 2030;
}

export function validarMes(mes: string): boolean {
  const month = parseInt(mes, 10);
  return !isNaN(month) && month >= 1 && month <= 12;
}