import type { Metadata } from 'next';
import { LegalPageShell } from '@/components/legal/legal-page-shell';

export const metadata: Metadata = {
  title: 'Política de Protección de Datos | Fatrans',
  description:
    'Política de tratamiento de datos personales conforme a la LOPDP venezolana y mejores prácticas internacionales.',
};

/**
 * Página /lopdp (issue #205) — Política de Protección de Datos Personales.
 *
 * Cubre los puntos mínimos del issue: qué datos se recopilan, finalidad,
 * con quién se comparten, derechos ARCO, contacto del DPO.
 *
 * El contenido es plantilla y requiere revisión legal — ver banner.
 */
export default function LopdpPage() {
  return (
    <LegalPageShell
      titulo="Política de Protección de Datos Personales"
      subtitulo="Tratamiento de datos conforme a la Ley Orgánica de Protección de Datos Personales (LOPDP) de Venezuela"
      version="1.0-borrador"
      ultimaActualizacion="17 de mayo de 2026"
      borrador
    >
      <h2>1. Responsable del tratamiento</h2>
      <p>
        Fatrans es el responsable del tratamiento de los datos personales
        recolectados a través de la plataforma. Para cualquier consulta,
        rectificación o solicitud relativa a sus datos personales, el
        titular puede contactar al Delegado de Protección de Datos (DPO) en{' '}
        <a href="mailto:dpo@fatrans.com.ve" className="text-[#16A34A] hover:underline">
          dpo@fatrans.com.ve
        </a>
        .
      </p>

      <h2>2. Datos que recopilamos</h2>
      <h3>2.1 Datos de identificación</h3>
      <ul>
        <li>Nombres y apellidos completos.</li>
        <li>Cédula de identidad o RIF, según corresponda.</li>
        <li>Fecha de nacimiento, nacionalidad y estado civil.</li>
        <li>Fotografía y captura biométrica facial (proceso KYC).</li>
      </ul>

      <h3>2.2 Datos de contacto</h3>
      <ul>
        <li>Correo electrónico, teléfono móvil y fijo.</li>
        <li>Dirección de residencia y dirección laboral.</li>
      </ul>

      <h3>2.3 Datos financieros y laborales</h3>
      <ul>
        <li>Ingresos declarados, fuente de ingresos, ocupación.</li>
        <li>
          Información de la unidad de transporte (placa, modelo, ruta) si
          aplica.
        </li>
        <li>Historial de operaciones realizadas en la plataforma.</li>
      </ul>

      <h3>2.4 Datos técnicos</h3>
      <ul>
        <li>Dirección IP, identificador de dispositivo, sistema operativo.</li>
        <li>Registros de actividad (logs) y sesiones de uso.</li>
        <li>Cookies — ver <a href="/cookies" className="text-[#16A34A] hover:underline">Política de Cookies</a>.</li>
      </ul>

      <h2>3. Finalidad del tratamiento</h2>
      <p>
        Sus datos personales serán tratados únicamente para las siguientes
        finalidades:
      </p>
      <ul>
        <li>
          <strong>Identificación y prevención de fraude:</strong> verificar
          la identidad del Socio (KYC) y prevenir operaciones ilícitas
          conforme a la normativa anti-lavado (LOCDOFT).
        </li>
        <li>
          <strong>Prestación del servicio:</strong> ejecutar las
          operaciones solicitadas (ahorro, crédito, depósitos, retiros).
        </li>
        <li>
          <strong>Cumplimiento normativo:</strong> reportar a SUDEBAN,
          SUNACRIP, SENIAT y demás autoridades cuando sea legalmente
          requerido.
        </li>
        <li>
          <strong>Mejora del servicio:</strong> análisis estadístico
          agregado y anonimizado para mejorar la plataforma.
        </li>
        <li>
          <strong>Comunicaciones operativas:</strong> envío de
          notificaciones transaccionales, estados de cuenta y avisos
          legales.
        </li>
      </ul>

      <h2>4. Base legal</h2>
      <p>El tratamiento se ampara en:</p>
      <ul>
        <li>El consentimiento expreso otorgado al aceptar esta política.</li>
        <li>La ejecución del contrato (T&amp;C).</li>
        <li>
          El cumplimiento de obligaciones legales aplicables al sector
          financiero venezolano.
        </li>
      </ul>

      <h2>5. ¿Con quién compartimos sus datos?</h2>
      <p>
        Sus datos personales no son vendidos ni cedidos a terceros para
        fines comerciales. Solo se comparten con:
      </p>
      <ul>
        <li>
          <strong>Autoridades competentes:</strong> SUDEBAN, SUNACRIP,
          SENIAT, UNIF, tribunales o cualquier ente público que lo requiera
          por mandato legal.
        </li>
        <li>
          <strong>Proveedores tecnológicos contratados:</strong>{' '}
          procesadores de pago, proveedores de hosting, servicios de envío
          de correo y SMS. Todos están vinculados por contratos de
          confidencialidad y tratamiento de datos.
        </li>
        <li>
          <strong>Centrales de riesgo:</strong> únicamente la información
          mínima necesaria para reportar mora o consultar comportamiento
          crediticio, previa autorización del titular.
        </li>
      </ul>

      <h2>6. Conservación</h2>
      <p>
        Conservamos sus datos durante el tiempo que mantenga la relación
        comercial con Fatrans y, posteriormente, por el plazo de diez (10)
        años exigido por la normativa financiera venezolana para fines de
        auditoría, fiscalización y prevención de lavado de activos.
        Transcurrido ese plazo, los datos serán bloqueados y, finalmente,
        eliminados.
      </p>

      <h2>7. Derechos del titular (ARCO + portabilidad)</h2>
      <p>
        Como titular de los datos, usted tiene derecho a:
      </p>
      <ul>
        <li>
          <strong>Acceso:</strong> conocer qué datos suyos tratamos y con
          qué finalidad.
        </li>
        <li>
          <strong>Rectificación:</strong> corregir datos inexactos o
          incompletos.
        </li>
        <li>
          <strong>Cancelación / supresión:</strong> solicitar que se
          eliminen sus datos, sujeto a los plazos legales de conservación.
        </li>
        <li>
          <strong>Oposición:</strong> oponerse al tratamiento para fines
          distintos a los esenciales del servicio.
        </li>
        <li>
          <strong>Portabilidad:</strong> recibir sus datos en un formato
          estructurado y legible por máquina.
        </li>
      </ul>
      <p>
        Para ejercer estos derechos, envíe una solicitud a{' '}
        <a href="mailto:dpo@fatrans.com.ve" className="text-[#16A34A] hover:underline">
          dpo@fatrans.com.ve
        </a>{' '}
        adjuntando copia de su documento de identidad. Responderemos en un
        plazo máximo de quince (15) días hábiles.
      </p>

      <h2>8. Medidas de seguridad</h2>
      <p>
        Aplicamos medidas técnicas y organizativas razonables para proteger
        sus datos: cifrado en tránsito (TLS 1.3), cifrado de credenciales
        en reposo (BCrypt), control de acceso por roles, registros de
        auditoría, y monitoreo de eventos sospechosos. En caso de incidente
        de seguridad que afecte sus datos, le notificaremos en los plazos
        legalmente exigidos.
      </p>

      <h2>9. Transferencias internacionales</h2>
      <p>
        Sus datos se almacenan en infraestructura ubicada principalmente en
        Venezuela. Si en el futuro fuera necesario transferirlos al
        exterior, se realizará únicamente con países que ofrezcan un nivel
        adecuado de protección y siempre bajo cláusulas contractuales que
        garanticen sus derechos.
      </p>

      <h2>10. Cambios a esta política</h2>
      <p>
        Cualquier modificación a esta política será notificada al titular
        por los medios registrados y, si conlleva cambios sustanciales en
        las finalidades del tratamiento, requerirá nuevo consentimiento
        expreso.
      </p>

      <h2>11. Contacto del DPO</h2>
      <p>
        Delegado de Protección de Datos —{' '}
        <a href="mailto:dpo@fatrans.com.ve" className="text-[#16A34A] hover:underline">
          dpo@fatrans.com.ve
        </a>
      </p>
    </LegalPageShell>
  );
}
