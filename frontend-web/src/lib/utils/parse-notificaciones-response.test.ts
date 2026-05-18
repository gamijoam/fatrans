import { describe, it, expect } from 'vitest';
import {
  parseNotificacionesResponse,
  formatearBadgeCount,
  estiloPorPrioridad,
  formatearTiempoRelativo,
  type NotificacionApi,
} from './parse-notificaciones-response';

/**
 * Tests para issue #214 PR-B: helpers de parsing y formato.
 */
describe('parse-notificaciones-response (issue #214 PR-B)', () => {
  const notif: NotificacionApi = {
    id: 'n1',
    tipo: 'KYC_APROBADO',
    titulo: 'Tu KYC fue aprobado',
    mensaje: 'Felicidades.',
    leida: false,
    prioridad: 'NORMAL',
    createdAt: '2026-05-17T10:00:00Z',
  };

  describe('parseNotificacionesResponse', () => {
    it('Formato real del backend → extrae fields correctos', () => {
      const respuesta = {
        notificaciones: [notif],
        pagina: 0,
        tamanio: 20,
        totalElementos: 1,
        totalPaginas: 1,
        noLeidas: 1,
      };

      const r = parseNotificacionesResponse(respuesta);
      expect(r.notificaciones).toHaveLength(1);
      expect(r.notificaciones[0].id).toBe('n1');
      expect(r.noLeidas).toBe(1);
    });

    it('Wrap con campos faltantes → completa con defaults razonables', () => {
      const r = parseNotificacionesResponse({ notificaciones: [notif] });
      expect(r.notificaciones).toHaveLength(1);
      expect(r.pagina).toBe(0);
      expect(r.totalElementos).toBe(1);
      expect(r.noLeidas).toBe(0);
    });

    it('Array directo (futuro-proof) → lo envuelve', () => {
      const r = parseNotificacionesResponse([notif]);
      expect(r.notificaciones).toHaveLength(1);
      expect(r.totalElementos).toBe(1);
    });

    it('null/undefined/string → payload vacío (UI muestra empty state)', () => {
      expect(parseNotificacionesResponse(null).notificaciones).toEqual([]);
      expect(parseNotificacionesResponse(undefined).notificaciones).toEqual([]);
      expect(parseNotificacionesResponse('error').notificaciones).toEqual([]);
    });

    it('Objeto con `notificaciones` no-array → vacío', () => {
      const r = parseNotificacionesResponse({ notificaciones: 'broken' });
      expect(r.notificaciones).toEqual([]);
    });
  });

  describe('formatearBadgeCount', () => {
    it('0 o negativo → null (no mostrar badge)', () => {
      expect(formatearBadgeCount(0)).toBeNull();
      expect(formatearBadgeCount(-5)).toBeNull();
    });

    it('1-99 → string del número', () => {
      expect(formatearBadgeCount(1)).toBe('1');
      expect(formatearBadgeCount(42)).toBe('42');
      expect(formatearBadgeCount(99)).toBe('99');
    });

    it('Issue #214: 100+ → "99+" (visual compacto)', () => {
      expect(formatearBadgeCount(100)).toBe('99+');
      expect(formatearBadgeCount(9999)).toBe('99+');
    });

    it('NaN o Infinity → null (defensive)', () => {
      expect(formatearBadgeCount(NaN)).toBeNull();
      expect(formatearBadgeCount(Infinity)).toBeNull();
    });
  });

  describe('estiloPorPrioridad', () => {
    it('URGENTE → "urgent" (rojo)', () => {
      expect(estiloPorPrioridad('URGENTE')).toBe('urgent');
    });

    it('NORMAL → "normal" (azul, default)', () => {
      expect(estiloPorPrioridad('NORMAL')).toBe('normal');
    });

    it('BAJA → "low" (gris)', () => {
      expect(estiloPorPrioridad('BAJA')).toBe('low');
    });

    it('Valor desconocido → "normal" (default seguro)', () => {
      expect(estiloPorPrioridad('FUTURO_VALOR')).toBe('normal');
    });
  });

  describe('formatearTiempoRelativo', () => {
    const ahora = new Date('2026-05-17T12:00:00Z');

    it('Hace menos de 1 minuto → "ahora mismo"', () => {
      const hace30s = new Date('2026-05-17T11:59:30Z').toISOString();
      expect(formatearTiempoRelativo(hace30s, ahora)).toBe('ahora mismo');
    });

    it('Hace 5 minutos → "hace 5 minutos"', () => {
      const hace5m = new Date('2026-05-17T11:55:00Z').toISOString();
      expect(formatearTiempoRelativo(hace5m, ahora)).toBe('hace 5 minutos');
    });

    it('Singular "1 minuto" (no "1 minutos")', () => {
      const hace1m = new Date('2026-05-17T11:59:00Z').toISOString();
      expect(formatearTiempoRelativo(hace1m, ahora)).toBe('hace 1 minuto');
    });

    it('Hace 3 horas → "hace 3 horas"', () => {
      const hace3h = new Date('2026-05-17T09:00:00Z').toISOString();
      expect(formatearTiempoRelativo(hace3h, ahora)).toBe('hace 3 horas');
    });

    it('Hace 3 días → "hace 3 días"', () => {
      const hace3d = new Date('2026-05-14T12:00:00Z').toISOString();
      expect(formatearTiempoRelativo(hace3d, ahora)).toBe('hace 3 días');
    });

    it('Más de 7 días → fecha localizada', () => {
      const hace15d = new Date('2026-05-01T12:00:00Z').toISOString();
      const r = formatearTiempoRelativo(hace15d, ahora);
      expect(r).toMatch(/\d{2}/);  // contiene día
    });

    it('Inputs inválidos → string vacío (defensive)', () => {
      expect(formatearTiempoRelativo('', ahora)).toBe('');
      expect(formatearTiempoRelativo('no-iso', ahora)).toBe('');
    });
  });
});
