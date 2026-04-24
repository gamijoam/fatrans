const ADMIN_ROLES = ['ADMIN', 'ADMINISTRADOR', 'GESTOR', 'SUPER_ADMIN'];

export interface AdminAuthContext {
  accessToken: string | undefined;
}

export function validateAdminAccess(context: AdminAuthContext): { valid: boolean; status: number; message: string } {
  if (!context.accessToken) {
    return { valid: false, status: 401, message: 'No autenticado' };
  }

  try {
    const payload = context.accessToken.split('.')[1];
    const decoded = Buffer.from(payload, 'base64').toString('utf-8');
    const data = JSON.parse(decoded);
    if (!ADMIN_ROLES.includes(data.rol)) {
      return { valid: false, status: 403, message: 'No autorizado' };
    }
    return { valid: true, status: 200, message: 'OK' };
  } catch {
    return { valid: false, status: 401, message: 'Token inválido' };
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