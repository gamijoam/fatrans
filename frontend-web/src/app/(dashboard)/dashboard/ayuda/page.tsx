'use client';

import { useState } from 'react';
import { Card, CardContent } from '@/components/ui/card';
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/accordion';
import {
  HelpCircle,
  Phone,
  Mail,
  Clock,
  ShieldAlert,
  MessageCircle,
  AlertTriangle,
  ChevronRight,
} from 'lucide-react';
import { FAQ_DATA, contarPreguntas, type FaqSeccion } from '@/lib/utils/faq-data';

/**
 * Centro de ayuda (issue #222). Página única con 3 secciones via tabs:
 * - FAQ por categorías (acordeón).
 * - Contacto: WhatsApp + email + horario.
 * - Reportar fraude: prioridad URGENTE con WhatsApp dedicado + email + checklist.
 *
 * Sin backend de tickets por ahora (issue separado para eso). Todo el flujo
 * es WhatsApp/mailto — MVP funcional desde día 1.
 */

type Tab = 'faq' | 'contacto' | 'fraude';

const TABS: { id: Tab; label: string; icon: typeof HelpCircle }[] = [
  { id: 'faq', label: 'Preguntas frecuentes', icon: HelpCircle },
  { id: 'contacto', label: 'Contacto', icon: MessageCircle },
  { id: 'fraude', label: 'Reportar fraude', icon: ShieldAlert },
];

// ⚠️ Datos de contacto reales — actualizar antes del lanzamiento.
const SOPORTE = {
  whatsapp: '+584121234567',
  whatsappTexto: 'Hola, necesito ayuda con mi cuenta Fatrans',
  email: 'soporte@fatrans.com.ve',
  horario: 'Lunes a Viernes, 8:00 AM - 6:00 PM (hora Venezuela)',
};

const FRAUDE = {
  whatsapp: '+584121234567', // mismo número, prioridad por mensaje URGENTE
  whatsappTexto: 'URGENTE: reportar posible fraude en mi cuenta Fatrans',
  email: 'fraude@fatrans.com.ve',
};

const wa = (numero: string, texto: string) =>
  `https://wa.me/${numero.replace(/[^0-9]/g, '')}?text=${encodeURIComponent(texto)}`;

export default function AyudaPage() {
  const [activeTab, setActiveTab] = useState<Tab>('faq');

  return (
    <div className="p-4 lg:p-6 max-w-5xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-[#0F2744]">Centro de ayuda</h1>
        <p className="text-sm text-slate-500 mt-1">
          Encuentra respuestas, contáctanos o reporta un fraude.
        </p>
      </div>

      {/* Tabs */}
      <div role="tablist" className="flex flex-wrap gap-2 border-b border-slate-200">
        {TABS.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              role="tab"
              aria-selected={isActive}
              onClick={() => setActiveTab(tab.id)}
              className={`inline-flex items-center gap-2 px-4 py-3 text-sm font-medium transition-colors border-b-2 -mb-px ${
                isActive
                  ? tab.id === 'fraude'
                    ? 'text-red-600 border-red-500'
                    : 'text-[#16A34A] border-[#16A34A]'
                  : 'text-slate-500 border-transparent hover:text-slate-700'
              }`}
            >
              <Icon className="w-4 h-4" />
              {tab.label}
            </button>
          );
        })}
      </div>

      {activeTab === 'faq' && <FaqSection />}
      {activeTab === 'contacto' && <ContactoSection />}
      {activeTab === 'fraude' && <FraudeSection />}
    </div>
  );
}

function FaqSection() {
  return (
    <div className="space-y-6">
      <p className="text-sm text-slate-500">
        {contarPreguntas()} preguntas frecuentes organizadas por categoría.
      </p>
      {FAQ_DATA.map((seccion) => (
        <FaqCategoria key={seccion.categoria} seccion={seccion} />
      ))}
    </div>
  );
}

function FaqCategoria({ seccion }: { seccion: FaqSeccion }) {
  return (
    <Card className="border-slate-200">
      <CardContent className="p-5">
        <div className="mb-4">
          <h3 className="font-semibold text-[#0F2744]">{seccion.titulo}</h3>
          <p className="text-xs text-slate-500 mt-0.5">{seccion.descripcion}</p>
        </div>
        <Accordion type="single" collapsible className="w-full">
          {seccion.preguntas.map((p, idx) => (
            <AccordionItem key={idx} value={`${seccion.categoria}-${idx}`}>
              <AccordionTrigger className="text-sm font-medium text-left hover:no-underline">
                {p.pregunta}
              </AccordionTrigger>
              <AccordionContent className="text-sm text-slate-600 leading-relaxed">
                {p.respuesta}
              </AccordionContent>
            </AccordionItem>
          ))}
        </Accordion>
      </CardContent>
    </Card>
  );
}

function ContactoSection() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      <a
        href={wa(SOPORTE.whatsapp, SOPORTE.whatsappTexto)}
        target="_blank"
        rel="noopener noreferrer"
        className="group"
      >
        <Card className="border-slate-200 hover:border-emerald-300 hover:shadow-md transition-all h-full">
          <CardContent className="p-6">
            <div className="w-12 h-12 rounded-xl bg-emerald-100 flex items-center justify-center mb-4">
              <MessageCircle className="w-6 h-6 text-emerald-600" />
            </div>
            <h3 className="font-semibold text-[#0F2744] mb-1">WhatsApp</h3>
            <p className="text-sm text-slate-600 mb-3">
              Atención rápida vía WhatsApp.
            </p>
            <p className="text-sm font-medium text-emerald-600 flex items-center gap-1 group-hover:gap-2 transition-all">
              {SOPORTE.whatsapp}
              <ChevronRight className="w-4 h-4" />
            </p>
          </CardContent>
        </Card>
      </a>

      <a href={`mailto:${SOPORTE.email}`} className="group">
        <Card className="border-slate-200 hover:border-blue-300 hover:shadow-md transition-all h-full">
          <CardContent className="p-6">
            <div className="w-12 h-12 rounded-xl bg-blue-100 flex items-center justify-center mb-4">
              <Mail className="w-6 h-6 text-blue-600" />
            </div>
            <h3 className="font-semibold text-[#0F2744] mb-1">Email</h3>
            <p className="text-sm text-slate-600 mb-3">
              Para consultas con detalle o adjuntos.
            </p>
            <p className="text-sm font-medium text-blue-600 flex items-center gap-1 group-hover:gap-2 transition-all">
              {SOPORTE.email}
              <ChevronRight className="w-4 h-4" />
            </p>
          </CardContent>
        </Card>
      </a>

      <Card className="border-slate-200 md:col-span-2">
        <CardContent className="p-6">
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 rounded-xl bg-slate-100 flex items-center justify-center flex-shrink-0">
              <Clock className="w-6 h-6 text-slate-600" />
            </div>
            <div>
              <h3 className="font-semibold text-[#0F2744] mb-1">
                Horario de atención
              </h3>
              <p className="text-sm text-slate-600">{SOPORTE.horario}</p>
              <p className="text-xs text-slate-500 mt-2">
                Fuera del horario, puedes dejar tu mensaje por WhatsApp o email
                y te responderemos el siguiente día hábil.
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

function FraudeSection() {
  return (
    <div className="space-y-4">
      {/* Banner de urgencia */}
      <div className="flex items-start gap-4 p-5 bg-red-50 border border-red-200 rounded-2xl">
        <div className="w-12 h-12 rounded-xl bg-red-100 flex items-center justify-center flex-shrink-0">
          <AlertTriangle className="w-6 h-6 text-red-600" />
        </div>
        <div>
          <h3 className="font-semibold text-red-900 mb-1">
            ¿Detectaste actividad sospechosa?
          </h3>
          <p className="text-sm text-red-800">
            Si sospechas que alguien accedió a tu cuenta o intenta engañarte,
            actúa <strong>ahora mismo</strong>. Cada minuto cuenta.
          </p>
        </div>
      </div>

      {/* Checklist de qué hacer YA */}
      <Card className="border-slate-200">
        <CardContent className="p-5">
          <h3 className="font-semibold text-[#0F2744] mb-4">Qué hacer YA</h3>
          <ol className="space-y-3 text-sm text-slate-700">
            <li className="flex gap-3">
              <span className="flex-shrink-0 w-6 h-6 rounded-full bg-red-100 text-red-700 font-bold text-xs flex items-center justify-center">
                1
              </span>
              <span>
                <strong>Cambia tu contraseña</strong> desde Mi Perfil &gt; Cambiar
                contraseña. Hazlo desde un dispositivo distinto al que sospechas
                que pudo verse comprometido.
              </span>
            </li>
            <li className="flex gap-3">
              <span className="flex-shrink-0 w-6 h-6 rounded-full bg-red-100 text-red-700 font-bold text-xs flex items-center justify-center">
                2
              </span>
              <span>
                <strong>Cierra todas tus sesiones</strong> (Mi Perfil &gt; Cerrar
                sesión en otros dispositivos).
              </span>
            </li>
            <li className="flex gap-3">
              <span className="flex-shrink-0 w-6 h-6 rounded-full bg-red-100 text-red-700 font-bold text-xs flex items-center justify-center">
                3
              </span>
              <span>
                <strong>Repórtanos por WhatsApp prioritario o email</strong> (botones
                abajo). Bloquearemos tu cuenta temporalmente y revisaremos las
                operaciones recientes.
              </span>
            </li>
          </ol>
        </CardContent>
      </Card>

      {/* CTAs de reporte */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <a
          href={wa(FRAUDE.whatsapp, FRAUDE.whatsappTexto)}
          target="_blank"
          rel="noopener noreferrer"
        >
          <Card className="border-red-200 hover:border-red-300 hover:shadow-md transition-all h-full bg-red-50/30">
            <CardContent className="p-6">
              <div className="w-12 h-12 rounded-xl bg-red-100 flex items-center justify-center mb-4">
                <Phone className="w-6 h-6 text-red-600" />
              </div>
              <h3 className="font-semibold text-red-900 mb-1">
                WhatsApp prioritario
              </h3>
              <p className="text-sm text-slate-700 mb-3">
                Respuesta inmediata para casos de fraude. El mensaje llega marcado
                como URGENTE.
              </p>
              <p className="text-sm font-bold text-red-600">{FRAUDE.whatsapp}</p>
            </CardContent>
          </Card>
        </a>

        <a href={`mailto:${FRAUDE.email}?subject=URGENTE%20reporte%20de%20fraude`}>
          <Card className="border-red-200 hover:border-red-300 hover:shadow-md transition-all h-full bg-red-50/30">
            <CardContent className="p-6">
              <div className="w-12 h-12 rounded-xl bg-red-100 flex items-center justify-center mb-4">
                <Mail className="w-6 h-6 text-red-600" />
              </div>
              <h3 className="font-semibold text-red-900 mb-1">
                Email de seguridad
              </h3>
              <p className="text-sm text-slate-700 mb-3">
                Para enviar capturas de pantalla u otra evidencia detallada.
              </p>
              <p className="text-sm font-bold text-red-600">{FRAUDE.email}</p>
            </CardContent>
          </Card>
        </a>
      </div>

      {/* Recordatorio anti-phishing */}
      <Card className="border-amber-200 bg-amber-50">
        <CardContent className="p-5">
          <h3 className="font-semibold text-amber-900 mb-2">
            Recuerda: Fatrans NUNCA te pedirá
          </h3>
          <ul className="text-sm text-amber-800 space-y-1 list-disc pl-5">
            <li>Tu contraseña por teléfono o WhatsApp</li>
            <li>Códigos de verificación que te lleguen por SMS</li>
            <li>Tus datos bancarios completos</li>
            <li>Que instales aplicaciones de control remoto</li>
          </ul>
          <p className="text-xs text-amber-700 mt-3">
            Si alguien te contacta pidiendo esto, es un fraude. Cuelga y repórtalo.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
