import { NextRequest, NextResponse } from 'next/server';
import { registroSchema } from '@/lib/utils/validators';
import { enforceOriginPolicy } from '@/lib/security/origin-check';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18080';

/**
 * Convierte el valor a BigDecimal-friendly string para el backend.
 * - Acepta number, string vacío, null, undefined.
 * - Devuelve null si no es un valor parseable.
 */
function normalizeSalario(value: unknown): string | null {
  if (value === null || value === undefined || value === '') {
    return null;
  }
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value.toString();
  }
  if (typeof value === 'string') {
    const cleaned = value.replace(',', '.').trim();
    if (cleaned === '') return null;
    const parsed = Number.parseFloat(cleaned);
    return Number.isFinite(parsed) ? parsed.toString() : null;
  }
  return null;
}

/**
 * Normaliza string opcional. Si llega '' o undefined, devuelve null (mejor para el backend
 * que diferencia null de string vacío en validaciones @Size/@Pattern).
 */
function emptyToNull(value: unknown): string | null {
  if (value === null || value === undefined) return null;
  if (typeof value !== 'string') return null;
  const trimmed = value.trim();
  return trimmed === '' ? null : trimmed;
}

/**
 * Extrae la IP del cliente respetando proxies (x-forwarded-for, x-real-ip).
 * Toma el primer hop de x-forwarded-for, que es el cliente original.
 */
function getClientIp(request: NextRequest): string | null {
  const xff = request.headers.get('x-forwarded-for');
  if (xff) {
    const first = xff.split(',')[0]?.trim();
    if (first) return first;
  }
  return request.headers.get('x-real-ip');
}

export async function POST(request: NextRequest) {
  // Whitelist centralizada en `lib/security/origin-check`. Antes vivía
  // inline en cada route, derivada de `NEXT_PUBLIC_*_URL` — y como esos
  // env vars se inlinen al BUILD (no runtime), bastaba con que el deploy
  // no pasara un build-arg para que el bundle quedara con la lista mal.
  // El 18-may-2026 los usuarios que entraban por `www.fatrans.com.ve` no
  // podían registrarse por eso.
  const blocked = enforceOriginPolicy(request);
  if (blocked) return blocked;

  try {
    const body = await request.json();

    // Defensa en profundidad: validar de nuevo con Zod aunque el form ya valida en cliente.
    const result = registroSchema.safeParse(body);
    if (!result.success) {
      return NextResponse.json(
        { message: 'Datos inválidos', errors: result.error.flatten().fieldErrors },
        { status: 400 }
      );
    }

    const data = result.data;

    // Construir el payload completo con TODOS los campos del schema, mapeados a los nombres
    // que espera SolicitudRegistroRequestDTO.java (campos camelCase idénticos).
    const backendPayload: Record<string, unknown> = {
      // --- Datos personales ---
      nombreCompleto: data.nombreCompleto,
      tipoDocumento: data.tipoDocumento,
      cedula: data.cedula,
      fechaNacimiento: data.fechaNacimiento, // YYYY-MM-DD → LocalDate en backend
      genero: data.genero,
      estadoCivil: data.estadoCivil,

      // --- Contacto ---
      correoElectronico: data.correoElectronico,
      telefono: data.telefono,

      // --- Información laboral ---
      empresa: data.empresa,
      rifEmpresa: emptyToNull(data.rifEmpresa),
      departamento: emptyToNull(data.departamento),
      cargo: emptyToNull(data.cargo),
      // El frontend puede enviar `salario` como string ('1500.00'), number (1500) o null
      // (el form lo convierte a number antes de POST). Normalizamos a string numérico
      // o null para que Jackson lo deserialice como BigDecimal.
      salario: normalizeSalario((body as Record<string, unknown>).salario ?? data.salario),

      // --- Dirección de residencia ---
      direccionEstado: emptyToNull(data.direccionEstado),
      direccionCiudad: emptyToNull(data.direccionCiudad),
      direccionMunicipio: emptyToNull(data.direccionMunicipio),
      direccionCalle: emptyToNull(data.direccionCalle),

      // --- Contacto de emergencia ---
      emergenciaNombre: emptyToNull(data.emergenciaNombre),
      emergenciaTelefono: emptyToNull(data.emergenciaTelefono),
      emergenciaParentesco: emptyToNull(data.emergenciaParentesco),

      // --- Consentimientos legales (booleanos, requeridos por el backend con @AssertTrue) ---
      aceptaTerminos: data.aceptaTerminos,
      aceptaLopdp: data.aceptaLopdp,
      // Issue #218 PR-B — declaración jurada LOCDOFT (origen lícito de fondos).
      aceptaLocdoft: data.aceptaLocdoft,

      // --- Auditoría LOPDP / LOCDOFT (defensa legal Venezuela) ---
      // IP del cliente original (respeta x-forwarded-for; el primer hop) y user-agent.
      // El backend trunca a 45 / 500 chars y, si vienen null/vacíos, hace fallback a
      // HttpServletRequest. Los timestamps consentLopdpTimestamp y consentLocdoftTimestamp
      // los sella el backend con Instant.now() cuando los flags llegan en true — NUNCA
      // confiamos en el cliente para timestamps.
      ipRegistro: getClientIp(request),
      userAgentRegistro: request.headers.get('user-agent'),
    };

    const backendResponse = await fetch(`${BACKEND_URL}/api/v1/socios/solicitud`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(backendPayload),
    });

    // 5xx → enmascarar como 502 genérico (no exponer detalles internos del backend)
    if (backendResponse.status >= 500) {
      // No loguear el body (puede contener PII reflejada). Solo status.
      console.error('Registro: backend 5xx', { status: backendResponse.status });
      return NextResponse.json(
        { message: 'Servicio temporalmente no disponible. Intenta de nuevo en unos minutos.' },
        { status: 502 }
      );
    }

    // 4xx → propagar el mensaje del backend (es seguro: son errores de validación de negocio)
    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({}));
      return NextResponse.json(
        { message: errorData.message || 'Error al procesar solicitud' },
        { status: backendResponse.status }
      );
    }

    return NextResponse.json(
      {
        success: true,
        message:
          'Solicitud de registro enviada correctamente. Recibirás un correo cuando un administrador la apruebe.',
      },
      { status: 201 }
    );
  } catch (error) {
    // No loguear el body (PII: cédula, teléfono, etc). Solo el error técnico.
    console.error('Registro error:', error instanceof Error ? error.message : 'unknown');
    return NextResponse.json({ message: 'Error interno del servidor' }, { status: 500 });
  }
}
