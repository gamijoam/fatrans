import { describe, it, expect } from 'vitest';
import {
  FAQ_DATA,
  contarPreguntas,
  buscarPreguntas,
  type FaqSeccion,
} from './faq-data';

/**
 * Tests para issue #222: datos del FAQ + helpers de búsqueda/conteo.
 *
 * Garantiza que:
 * - El FAQ tiene contenido sustantivo (no dummy).
 * - Las categorías esperadas están presentes.
 * - La búsqueda funciona case-insensitive.
 */
describe('faq-data (issue #222)', () => {

  describe('FAQ_DATA contiene contenido autoritativo', () => {
    it('hay al menos 5 categorías', () => {
      expect(FAQ_DATA.length).toBeGreaterThanOrEqual(5);
    });

    it('cada sección tiene título, descripción y preguntas', () => {
      FAQ_DATA.forEach((sec) => {
        expect(sec.titulo).toBeTruthy();
        expect(sec.descripcion).toBeTruthy();
        expect(sec.preguntas.length).toBeGreaterThan(0);
      });
    });

    it('cada pregunta tiene pregunta y respuesta no vacías', () => {
      FAQ_DATA.forEach((sec) => {
        sec.preguntas.forEach((p) => {
          expect(p.pregunta.length).toBeGreaterThan(5);
          expect(p.respuesta.length).toBeGreaterThan(20);
        });
      });
    });

    it('Issue #222: categorías esperadas presentes', () => {
      const categorias = FAQ_DATA.map((s) => s.categoria);
      expect(categorias).toContain('cuenta');
      expect(categorias).toContain('kyc');
      expect(categorias).toContain('creditos');
      expect(categorias).toContain('depositos-retiros');
      expect(categorias).toContain('seguridad');
    });

    it('Issue #222: FAQ de seguridad incluye instrucciones anti-phishing', () => {
      const seguridad = FAQ_DATA.find((s) => s.categoria === 'seguridad');
      expect(seguridad).toBeDefined();
      const textos = seguridad!.preguntas
        .map((p) => p.respuesta.toLowerCase())
        .join(' ');
      // Verificación crítica: Fatrans NUNCA pide contraseña por teléfono
      expect(textos).toContain('nunca');
      expect(textos).toMatch(/contraseña|password/);
    });
  });

  describe('contarPreguntas', () => {
    it('cuenta correctamente todas las preguntas', () => {
      const total = contarPreguntas();
      // Verifico que sea coherente con la suma manual
      const manual = FAQ_DATA.reduce((acc, s) => acc + s.preguntas.length, 0);
      expect(total).toBe(manual);
      expect(total).toBeGreaterThan(10); // mínimo razonable
    });

    it('retorna 0 con array vacío', () => {
      expect(contarPreguntas([])).toBe(0);
    });

    it('cuenta secciones custom', () => {
      const custom: FaqSeccion[] = [
        {
          categoria: 'cuenta',
          titulo: 'X',
          descripcion: 'Y',
          preguntas: [
            { pregunta: 'a', respuesta: 'b' },
            { pregunta: 'c', respuesta: 'd' },
          ],
        },
      ];
      expect(contarPreguntas(custom)).toBe(2);
    });
  });

  describe('buscarPreguntas', () => {
    it('encuentra preguntas que mencionan KYC', () => {
      const result = buscarPreguntas('kyc');
      expect(result.length).toBeGreaterThan(0);
      result.forEach((p) => {
        const combinado = (p.pregunta + ' ' + p.respuesta).toLowerCase();
        expect(combinado).toContain('kyc');
      });
    });

    it('case-insensitive', () => {
      const lower = buscarPreguntas('contraseña');
      const upper = buscarPreguntas('CONTRASEÑA');
      expect(lower.length).toBe(upper.length);
      expect(lower.length).toBeGreaterThan(0);
    });

    it('texto vacío → array vacío', () => {
      expect(buscarPreguntas('')).toEqual([]);
      expect(buscarPreguntas('   ')).toEqual([]);
    });

    it('texto sin matches → array vacío', () => {
      expect(buscarPreguntas('xyzwxyzwxyz123')).toEqual([]);
    });

    it('busca también en respuestas (no solo en preguntas)', () => {
      // "fraude" probablemente aparece en respuesta, no en pregunta exacta
      const result = buscarPreguntas('fraude');
      expect(result.length).toBeGreaterThan(0);
    });
  });
});
