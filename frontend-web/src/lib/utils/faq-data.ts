/**
 * Datos del FAQ del centro de ayuda (issue #222).
 *
 * Hardcoded por ahora (autoritativo: lo escribimos nosotros con base en
 * preguntas reales de socios). Cuando se prioriza un sistema de tickets/CMS,
 * esto puede migrar a un fetch desde backend, pero para arrancar la versión
 * estática es más rápida, mantenible y no requiere infra nueva.
 *
 * Estructura puede serializarse a JSON fácil si se migra a CMS.
 */

export type FaqCategoria =
  | 'cuenta'
  | 'kyc'
  | 'creditos'
  | 'depositos-retiros'
  | 'seguridad';

export interface FaqPregunta {
  pregunta: string;
  respuesta: string;
}

export interface FaqSeccion {
  categoria: FaqCategoria;
  titulo: string;
  descripcion: string;
  preguntas: FaqPregunta[];
}

export const FAQ_DATA: ReadonlyArray<FaqSeccion> = [
  {
    categoria: 'cuenta',
    titulo: 'Mi cuenta',
    descripcion: 'Información general sobre tu cuenta en Fatrans',
    preguntas: [
      {
        pregunta: '¿Cómo me registro en Fatrans?',
        respuesta:
          'Desde la página principal, haz clic en "Crear cuenta" y completa el formulario con tus datos personales (cédula, fecha de nacimiento, datos de empresa). Debes tener al menos 18 años. Tras el registro inicial, deberás verificar tu identidad (KYC) para poder operar.',
      },
      {
        pregunta: '¿Por qué me piden datos de mi empresa?',
        respuesta:
          'Fatrans es un fondo de ahorro para transportistas afiliados. Verificamos tu vínculo laboral con una empresa del sector como parte de la afiliación al fondo.',
      },
      {
        pregunta: '¿Puedo cambiar mi correo electrónico o teléfono?',
        respuesta:
          'Sí. Ve a Mi Perfil > Datos de contacto. Por seguridad, te pediremos confirmar tu contraseña actual antes de guardar cambios sensibles.',
      },
      {
        pregunta: '¿Cómo cierro mi cuenta?',
        respuesta:
          'Contacta a soporte por WhatsApp o email. El cierre de cuenta requiere validación adicional (saldos, créditos vigentes, etc.) y se procesa en 5-7 días hábiles.',
      },
    ],
  },
  {
    categoria: 'kyc',
    titulo: 'Verificación de identidad (KYC)',
    descripcion: 'Por qué la verificación es obligatoria y cómo completarla',
    preguntas: [
      {
        pregunta: '¿Qué es el KYC y por qué es obligatorio?',
        respuesta:
          'KYC (Know Your Customer) es el proceso de verificación de identidad que exige la regulación bancaria venezolana. Sin KYC aprobado, las operaciones están limitadas: no puedes solicitar créditos ni realizar depósitos/retiros sobre cierto monto.',
      },
      {
        pregunta: '¿Cuánto tarda la aprobación del KYC?',
        respuesta:
          'Una vez subidos todos los documentos requeridos, la revisión toma entre 24 y 48 horas hábiles. Te notificaremos por correo y verás el estado actualizado en tu dashboard.',
      },
      {
        pregunta: 'Mi KYC fue rechazado, ¿qué hago?',
        respuesta:
          'En tu dashboard verás un banner rojo con el motivo del rechazo. Ve a la sección KYC, corrige los documentos según el motivo indicado y reenvía. La nueva revisión toma 24-48h adicionales.',
      },
      {
        pregunta: '¿Qué documentos necesito?',
        respuesta:
          'Cédula de identidad vigente (frente y reverso), un selfie sosteniendo tu cédula, comprobante de domicilio (no mayor a 3 meses) y constancia de trabajo de tu empresa.',
      },
    ],
  },
  {
    categoria: 'depositos-retiros',
    titulo: 'Depósitos y retiros',
    descripcion: 'Cómo aportar y retirar tu dinero',
    preguntas: [
      {
        pregunta: '¿Cómo aporto a mi cuenta de ahorro?',
        respuesta:
          'Tu empresa descuenta tu aporte mensualmente de tu nómina y lo deposita en tu cuenta Fatrans. También puedes hacer depósitos voluntarios desde "Cuentas > Depositar" en tu dashboard.',
      },
      {
        pregunta: '¿Cuándo puedo retirar mi dinero?',
        respuesta:
          'Puedes solicitar retiros parciales en cualquier momento, sujeto a los límites de tu nivel de KYC. Los retiros se procesan en 24-72 horas hábiles según el método elegido.',
      },
      {
        pregunta: '¿En qué monedas opera Fatrans?',
        respuesta:
          'Bolívares (VES) y dólares estadounidenses (USD). Cada cuenta es independiente y los saldos se muestran tanto en su moneda original como agregados en Bs usando la tasa BCV del día.',
      },
    ],
  },
  {
    categoria: 'creditos',
    titulo: 'Créditos',
    descripcion: 'Solicitudes, plazos y requisitos',
    preguntas: [
      {
        pregunta: '¿Quién puede solicitar un crédito?',
        respuesta:
          'Socios con KYC aprobado y al menos 3 meses de aportes consecutivos. El monto y plazo dependen del tipo de crédito (educación, micro, vehículo).',
      },
      {
        pregunta: '¿Cuál es la tasa de interés?',
        respuesta:
          'Las tasas son competitivas y se actualizan periódicamente. Consulta el simulador en Créditos > Simular para ver la tasa actual de cada tipo de crédito antes de solicitar.',
      },
      {
        pregunta: '¿En cuánto tiempo me aprueban?',
        respuesta:
          'Tras enviar la solicitud, un analista de crédito evalúa tu capacidad de pago y respuesta crediticia en 1-3 días hábiles. Si es aprobado, el desembolso ocurre en las siguientes 24h.',
      },
    ],
  },
  {
    categoria: 'seguridad',
    titulo: 'Seguridad',
    descripcion: 'Cómo proteger tu cuenta y qué hacer ante un incidente',
    preguntas: [
      {
        pregunta: '¿Mi sesión se cierra sola?',
        respuesta:
          'Sí. Por tu seguridad, tu sesión se cierra automáticamente tras 10 minutos sin actividad. Verás una advertencia con 60 segundos para confirmar que sigues conectado.',
      },
      {
        pregunta: '¿Cómo cambio mi contraseña?',
        respuesta:
          'Ve a Mi Perfil > Cambiar contraseña. Te pediremos tu contraseña actual y la nueva debe cumplir los requisitos de complejidad (mínimo 8 caracteres, mayúsculas, números y símbolos).',
      },
      {
        pregunta: '¿Qué hago si sospecho que alguien accedió a mi cuenta?',
        respuesta:
          'Cambia tu contraseña INMEDIATAMENTE y luego repórtanos por WhatsApp prioritario o email a fraude@fatrans.com.ve. Bloquearemos tu cuenta temporalmente y revisaremos las operaciones recientes.',
      },
      {
        pregunta: '¿Fatrans me pedirá mi contraseña por teléfono o WhatsApp?',
        respuesta:
          'NUNCA. Si alguien te contacta pidiendo tu contraseña, código de verificación o datos de tu cuenta, es un fraude. Cuelga y reporta al número de WhatsApp prioritario.',
      },
    ],
  },
];

/**
 * Cuenta el total de preguntas en todas las secciones.
 * Usado para tests y mostrar contador en UI.
 */
export function contarPreguntas(secciones: ReadonlyArray<FaqSeccion> = FAQ_DATA): number {
  return secciones.reduce((total, sec) => total + sec.preguntas.length, 0);
}

/**
 * Busca preguntas que contengan el texto (case-insensitive).
 * Para futura búsqueda en el FAQ.
 */
export function buscarPreguntas(
  texto: string,
  secciones: ReadonlyArray<FaqSeccion> = FAQ_DATA
): FaqPregunta[] {
  if (!texto || texto.trim().length === 0) return [];
  const q = texto.toLowerCase().trim();
  return secciones.flatMap((sec) =>
    sec.preguntas.filter(
      (p) =>
        p.pregunta.toLowerCase().includes(q) ||
        p.respuesta.toLowerCase().includes(q)
    )
  );
}
