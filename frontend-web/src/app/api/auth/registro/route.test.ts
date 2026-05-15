import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { POST } from './route';

/**
 * Helper para construir un NextRequest-like mock. La ruta solo usa:
 *  - request.headers.get(name)
 *  - request.json()
 */
function buildRequest({
  body,
  origin = 'http://localhost:3000',
  referer = 'http://localhost:3000/registro',
  xForwardedFor = '203.0.113.42, 10.0.0.1',
  userAgent = 'Mozilla/5.0 (Test)',
}: {
  body: unknown;
  origin?: string | null;
  referer?: string | null;
  xForwardedFor?: string | null;
  userAgent?: string | null;
}): any {
  const headers = new Map<string, string>();
  if (origin) headers.set('origin', origin);
  if (referer) headers.set('referer', referer);
  if (xForwardedFor) headers.set('x-forwarded-for', xForwardedFor);
  if (userAgent) headers.set('user-agent', userAgent);

  return {
    headers: {
      get: (name: string) => headers.get(name.toLowerCase()) ?? null,
    },
    json: async () => body,
  };
}

/**
 * Payload válido y completo, alineado con `registroSchema`.
 */
function buildValidPayload(overrides: Record<string, unknown> = {}): Record<string, unknown> {
  return {
    nombreCompleto: 'Juan Pérez García',
    tipoDocumento: 'CEDULA',
    cedula: 'V-12345678',
    fechaNacimiento: '1990-05-15',
    genero: 'MASCULINO',
    estadoCivil: 'SOLTERO',
    correoElectronico: 'juan@ejemplo.com',
    telefono: '04121234567',
    empresa: 'Acme Corp',
    rifEmpresa: 'J-12345678-9',
    departamento: 'Sistemas',
    cargo: 'Desarrollador',
    salario: '1500.50',
    direccionEstado: 'Distrito Capital',
    direccionCiudad: 'Caracas',
    direccionMunicipio: 'Libertador',
    direccionCalle: 'Av. Principal #123',
    emergenciaNombre: 'María García',
    emergenciaTelefono: '04141234567',
    emergenciaParentesco: 'Cónyuge',
    aceptaTerminos: true,
    aceptaLopdp: true,
    ...overrides,
  };
}

describe('POST /api/auth/registro (BFF)', () => {
  let fetchSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    fetchSpy = vi.spyOn(global, 'fetch');
    // Silenciar console.error en tests (los assertions sobre console no son el foco aquí).
    vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('reenvía TODOS los campos del schema al backend con un payload válido', async () => {
    fetchSpy.mockResolvedValue(
      new Response(JSON.stringify({ ok: true }), { status: 201 }) as any
    );

    const payload = buildValidPayload();
    const req = buildRequest({ body: payload });

    const res = await POST(req);
    expect(res.status).toBe(201);
    const json = await res.json();
    expect(json.success).toBe(true);

    expect(fetchSpy).toHaveBeenCalledTimes(1);
    const [, init] = fetchSpy.mock.calls[0] as [string, RequestInit];
    expect(init.method).toBe('POST');
    expect((init.headers as Record<string, string>)['Content-Type']).toBe('application/json');

    const forwarded = JSON.parse(init.body as string);

    // Lista canónica de campos que DEBEN reenviarse (22 campos del schema).
    const expectedKeys = [
      'nombreCompleto',
      'tipoDocumento',
      'cedula',
      'fechaNacimiento',
      'genero',
      'estadoCivil',
      'correoElectronico',
      'telefono',
      'empresa',
      'rifEmpresa',
      'departamento',
      'cargo',
      'salario',
      'direccionEstado',
      'direccionCiudad',
      'direccionMunicipio',
      'direccionCalle',
      'emergenciaNombre',
      'emergenciaTelefono',
      'emergenciaParentesco',
      'aceptaTerminos',
      'aceptaLopdp',
    ];
    for (const key of expectedKeys) {
      expect(forwarded).toHaveProperty(key);
    }

    // Valores requeridos llegan tal cual.
    expect(forwarded.nombreCompleto).toBe('Juan Pérez García');
    expect(forwarded.tipoDocumento).toBe('CEDULA');
    expect(forwarded.cedula).toBe('V-12345678');
    expect(forwarded.fechaNacimiento).toBe('1990-05-15');
    expect(forwarded.genero).toBe('MASCULINO');
    expect(forwarded.estadoCivil).toBe('SOLTERO');
    expect(forwarded.correoElectronico).toBe('juan@ejemplo.com');
    expect(forwarded.telefono).toBe('04121234567');
    expect(forwarded.empresa).toBe('Acme Corp');

    // Booleanos de consentimiento llegan como `true`.
    expect(forwarded.aceptaTerminos).toBe(true);
    expect(forwarded.aceptaLopdp).toBe(true);

    // Salario normalizado a string numérico para BigDecimal.
    expect(forwarded.salario).toBe('1500.5');

    // Opcionales con valor llegan trimmed.
    expect(forwarded.direccionEstado).toBe('Distrito Capital');
    expect(forwarded.emergenciaParentesco).toBe('Cónyuge');
  });

  it('reenvía ipRegistro (primer hop de x-forwarded-for) y userAgentRegistro al backend (LOPDP)', async () => {
    fetchSpy.mockResolvedValue(
      new Response(JSON.stringify({ ok: true }), { status: 201 }) as any
    );

    const req = buildRequest({
      body: buildValidPayload(),
      xForwardedFor: '203.0.113.42, 10.0.0.1, 172.16.0.1',
      userAgent: 'Mozilla/5.0 (TestLopdpAgent)',
    });

    const res = await POST(req);
    expect(res.status).toBe(201);

    const [, init] = fetchSpy.mock.calls[0] as [string, RequestInit];
    const forwarded = JSON.parse(init.body as string);

    // El BFF debe tomar el PRIMER hop (cliente real), no los proxies intermedios.
    expect(forwarded.ipRegistro).toBe('203.0.113.42');
    expect(forwarded.userAgentRegistro).toBe('Mozilla/5.0 (TestLopdpAgent)');
  });

  it('envía ipRegistro=null cuando no hay headers de IP (BFF directo) — backend hace fallback', async () => {
    fetchSpy.mockResolvedValue(
      new Response(JSON.stringify({ ok: true }), { status: 201 }) as any
    );

    const req = buildRequest({
      body: buildValidPayload(),
      xForwardedFor: null,
      userAgent: null,
    });

    const res = await POST(req);
    expect(res.status).toBe(201);

    const [, init] = fetchSpy.mock.calls[0] as [string, RequestInit];
    const forwarded = JSON.parse(init.body as string);

    // Sin headers el BFF manda null y deja que el backend caiga a HttpServletRequest.getRemoteAddr().
    expect(forwarded).toHaveProperty('ipRegistro');
    expect(forwarded.ipRegistro).toBeNull();
    expect(forwarded.userAgentRegistro).toBeNull();
  });

  it('rechaza salario como number con 400 (Zod requiere string) — documenta el bug G2 del form', async () => {
    // El form (`registration-form.tsx`) convierte salario a number con parseFloat antes del POST.
    // Pero `registroSchema.salario` espera un string. Zod rechaza → 400.
    // Este test fija el comportamiento esperado HOY; cuando se arregle G2 (form o schema),
    // este test deberá invertirse para validar que el number se acepta.
    const payload = { ...buildValidPayload(), salario: 1500.5 };
    const req = buildRequest({ body: payload });

    const res = await POST(req);
    expect(res.status).toBe(400);
    const json = await res.json();
    expect(json.message).toBe('Datos inválidos');
    expect(json.errors).toHaveProperty('salario');
    expect(fetchSpy).not.toHaveBeenCalled();
  });

  it('convierte campos opcionales vacíos a null', async () => {
    fetchSpy.mockResolvedValue(new Response('{}', { status: 201 }) as any);

    const payload = buildValidPayload({
      rifEmpresa: '',
      departamento: '',
      cargo: '',
      salario: '',
      direccionEstado: '',
      direccionCiudad: '',
      direccionMunicipio: '',
      direccionCalle: '',
      emergenciaNombre: '',
      emergenciaTelefono: '',
      emergenciaParentesco: '',
    });
    const req = buildRequest({ body: payload });

    await POST(req);

    const [, init] = fetchSpy.mock.calls[0] as [string, RequestInit];
    const forwarded = JSON.parse(init.body as string);

    expect(forwarded.rifEmpresa).toBeNull();
    expect(forwarded.departamento).toBeNull();
    expect(forwarded.cargo).toBeNull();
    expect(forwarded.salario).toBeNull();
    expect(forwarded.direccionEstado).toBeNull();
    expect(forwarded.direccionCalle).toBeNull();
    expect(forwarded.emergenciaNombre).toBeNull();
  });

  it('devuelve 403 cuando el origin no está permitido', async () => {
    const req = buildRequest({
      body: buildValidPayload(),
      origin: 'http://evil.example.com',
    });

    const res = await POST(req);

    expect(res.status).toBe(403);
    const json = await res.json();
    expect(json.message).toBe('Origen no permitido');
    expect(fetchSpy).not.toHaveBeenCalled();
  });

  it('devuelve 403 cuando el referer no está permitido', async () => {
    const req = buildRequest({
      body: buildValidPayload(),
      origin: null,
      referer: 'http://evil.example.com/registro',
    });

    const res = await POST(req);

    expect(res.status).toBe(403);
    const json = await res.json();
    expect(json.message).toBe('Referer no permitido');
    expect(fetchSpy).not.toHaveBeenCalled();
  });

  it('devuelve 400 con detalle cuando el body falla la validación Zod', async () => {
    const req = buildRequest({
      body: {
        nombreCompleto: 'Ab', // muy corto
        cedula: 'INVALIDO',
        correoElectronico: 'no-es-email',
        // faltan los demás campos requeridos
      },
    });

    const res = await POST(req);

    expect(res.status).toBe(400);
    const json = await res.json();
    expect(json.message).toBe('Datos inválidos');
    expect(json.errors).toBeDefined();
    expect(typeof json.errors).toBe('object');
    // Debe contener al menos algún campo con errores específicos.
    expect(Object.keys(json.errors).length).toBeGreaterThan(0);
    expect(fetchSpy).not.toHaveBeenCalled();
  });

  it('propaga el status y mensaje cuando el backend devuelve 4xx', async () => {
    fetchSpy.mockResolvedValue(
      new Response(JSON.stringify({ message: 'Cédula ya registrada' }), {
        status: 409,
      }) as any
    );

    const req = buildRequest({ body: buildValidPayload() });
    const res = await POST(req);

    expect(res.status).toBe(409);
    const json = await res.json();
    expect(json.message).toBe('Cédula ya registrada');
  });

  it('propaga 400 del backend con mensaje por defecto si el body no es JSON', async () => {
    fetchSpy.mockResolvedValue(
      new Response('texto plano', { status: 400 }) as any
    );

    const req = buildRequest({ body: buildValidPayload() });
    const res = await POST(req);

    expect(res.status).toBe(400);
    const json = await res.json();
    expect(json.message).toBe('Error al procesar solicitud');
  });

  it('enmascara 5xx del backend como 502 genérico', async () => {
    fetchSpy.mockResolvedValue(
      new Response(JSON.stringify({ message: 'Database down', stack: 'PII...' }), {
        status: 500,
      }) as any
    );

    const req = buildRequest({ body: buildValidPayload() });
    const res = await POST(req);

    expect(res.status).toBe(502);
    const json = await res.json();
    // No expone el mensaje interno del backend.
    expect(json.message).not.toContain('Database');
    expect(json.message).toMatch(/no disponible|intenta de nuevo/i);
  });

  it('devuelve 500 si fetch lanza error de red', async () => {
    fetchSpy.mockRejectedValue(new Error('ECONNREFUSED'));

    const req = buildRequest({ body: buildValidPayload() });
    const res = await POST(req);

    expect(res.status).toBe(500);
    const json = await res.json();
    expect(json.message).toBe('Error interno del servidor');
  });

  it('permite peticiones sin origin ni referer (server-to-server)', async () => {
    fetchSpy.mockResolvedValue(new Response('{}', { status: 201 }) as any);

    const req = buildRequest({
      body: buildValidPayload(),
      origin: null,
      referer: null,
    });
    const res = await POST(req);

    expect(res.status).toBe(201);
    expect(fetchSpy).toHaveBeenCalledTimes(1);
  });
});
