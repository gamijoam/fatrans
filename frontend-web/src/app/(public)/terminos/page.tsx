import type { Metadata } from 'next';
import { LegalPageShell } from '@/components/legal/legal-page-shell';

export const metadata: Metadata = {
  title: 'Términos y Condiciones | Fatrans',
  description:
    'Términos y condiciones de uso de la plataforma Fatrans — fondo de ahorro para el sector transporte venezolano.',
};

/**
 * Página /terminos (issue #205).
 *
 * El contenido es una PLANTILLA estructurada que cubre los puntos mínimos
 * exigidos por el issue (objeto, obligaciones, comisiones, mora,
 * jurisdicción, contacto). Debe ser revisado por un abogado antes de pasar
 * a producción — ver banner `borrador` en el shell.
 */
export default function TerminosPage() {
  return (
    <LegalPageShell
      titulo="Términos y Condiciones de Uso"
      subtitulo="Plataforma Fatrans — Fondo de Ahorro para el Sector Transporte"
      version="1.0-borrador"
      ultimaActualizacion="17 de mayo de 2026"
      borrador
    >
      <h2>1. Aceptación de los términos</h2>
      <p>
        Al registrarse en la plataforma Fatrans (en adelante, "la
        Plataforma") y aceptar los presentes Términos y Condiciones (en
        adelante, "T&amp;C"), el usuario (en adelante, "el Socio")
        manifiesta haberlos leído, entendido y aceptado en su totalidad,
        comprometiéndose a cumplirlos. Si el Socio no está de acuerdo con
        alguna de las disposiciones, deberá abstenerse de usar la
        Plataforma.
      </p>
      <p>
        Estos T&amp;C constituyen un contrato entre el Socio y Fatrans
        regido por la legislación de la República Bolivariana de Venezuela.
      </p>

      <h2>2. Objeto del contrato</h2>
      <p>
        Fatrans es una plataforma digital que ofrece a personas vinculadas
        al sector transporte (conductores, propietarios, mecánicos y
        empresas de transporte) los siguientes servicios:
      </p>
      <ul>
        <li>Apertura y administración de cuentas de ahorro en VES y USD.</li>
        <li>Acceso a productos crediticios sujetos a evaluación.</li>
        <li>
          Operaciones de depósito, retiro y transferencia entre cuentas
          autorizadas.
        </li>
        <li>Servicios de gestión administrativa de la unidad de transporte.</li>
      </ul>

      <h2>3. Requisitos para ser Socio</h2>
      <ul>
        <li>Ser persona natural mayor de edad o persona jurídica legalmente constituida.</li>
        <li>
          Poseer documento de identidad válido (cédula venezolana o RIF) y
          completar el proceso de verificación de identidad (KYC).
        </li>
        <li>
          Aceptar expresamente estos T&amp;C, la Política de Protección de
          Datos y la Política de Cookies.
        </li>
        <li>
          Acreditar vinculación con el sector transporte según los criterios
          publicados por Fatrans.
        </li>
      </ul>

      <h2>4. Obligaciones del Socio</h2>
      <ul>
        <li>
          Suministrar información veraz, completa y actualizada en el
          registro y durante toda la relación.
        </li>
        <li>
          Custodiar sus credenciales de acceso (usuario, contraseña, OTP) y
          notificar de inmediato cualquier uso no autorizado.
        </li>
        <li>
          Usar la Plataforma exclusivamente para fines lícitos y no realizar
          operaciones que constituyan o faciliten lavado de activos,
          financiamiento al terrorismo, fraude o cualquier delito tipificado
          en la legislación venezolana.
        </li>
        <li>Mantener actualizados sus datos personales y de contacto.</li>
        <li>
          Pagar puntualmente las cuotas de los créditos otorgados según el
          plan de amortización suscrito.
        </li>
      </ul>

      <h2>5. Comisiones y cargos</h2>
      <p>
        Las comisiones aplicables a cada operación (apertura, mantenimiento,
        transferencias, desembolsos, prepagos, mora, etc.) se publican en el
        tarifario vigente, disponible dentro de la Plataforma y actualizado
        periódicamente. Fatrans notificará a los Socios cualquier
        modificación con al menos quince (15) días de anticipación a su
        entrada en vigor.
      </p>

      <h2>6. Política de mora</h2>
      <p>
        El incumplimiento en el pago de cualquier cuota de un crédito
        generará intereses de mora calculados conforme a la tasa máxima
        permitida por el Banco Central de Venezuela vigente al momento del
        atraso. Adicionalmente, Fatrans podrá:
      </p>
      <ul>
        <li>Reportar la mora a las centrales de riesgo competentes.</li>
        <li>Suspender el acceso del Socio a nuevos productos crediticios.</li>
        <li>
          Iniciar las acciones de cobranza extrajudicial o judicial que
          correspondan.
        </li>
      </ul>

      <h2>7. Limitación de responsabilidad</h2>
      <p>
        Fatrans empleará razonables esfuerzos para mantener la Plataforma
        operativa y segura. No obstante, no se hará responsable por daños
        derivados de:
      </p>
      <ul>
        <li>Fallas de conectividad o energía ajenas a su control.</li>
        <li>
          Uso indebido de las credenciales del Socio por terceros cuando se
          deba a negligencia del propio Socio.
        </li>
        <li>Eventos de fuerza mayor o caso fortuito.</li>
      </ul>

      <h2>8. Modificaciones</h2>
      <p>
        Fatrans podrá modificar estos T&amp;C en cualquier momento. Las
        modificaciones serán comunicadas al Socio por los medios
        registrados (correo, notificación in-app) y, si conllevan cambios
        sustanciales, requerirán re-aceptación expresa al siguiente inicio
        de sesión.
      </p>

      <h2>9. Terminación</h2>
      <p>
        El Socio podrá solicitar la terminación de la relación en cualquier
        momento, siempre que haya cumplido con todas sus obligaciones
        pendientes. Fatrans podrá suspender o cancelar la cuenta del Socio
        en caso de incumplimiento grave, fraude o cuando sea requerido por
        autoridad competente.
      </p>

      <h2>10. Resolución de disputas y jurisdicción</h2>
      <p>
        Las partes intentarán resolver cualquier controversia derivada de
        estos T&amp;C de buena fe. Si no fuera posible, las partes se
        someten a la jurisdicción de los tribunales competentes de la
        República Bolivariana de Venezuela.
      </p>

      <h2>11. Contacto</h2>
      <p>
        Para cualquier consulta sobre estos T&amp;C, el Socio puede
        escribir a{' '}
        <a href="mailto:soporte@fatrans.com.ve" className="text-[#16A34A] hover:underline">
          soporte@fatrans.com.ve
        </a>
        .
      </p>
    </LegalPageShell>
  );
}
