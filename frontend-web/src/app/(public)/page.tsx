import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Fondo de Ahorro Platform | Digitaliza y Escala tu Cooperativa",
  description:
    "Plataforma web integral para gestionar socios, ahorros y créditos. Cumplimiento LOPDP/SUDEBAN. Arquitectura escalable a 20k+ usuarios.",
  openGraph: {
    title: "Fondo de Ahorro Platform",
    description: "Digitaliza, automatiza y escala tu fondo de ahorro desde la web.",
    type: "website",
  },
};

// ==========================================
// DATOS ESTÁTICOS (Fácil de migrar a CMS luego)
// ==========================================
const FEATURES = [
  {
    icon: "👥",
    title: "Gestión de Socios",
    desc: "Expediente digital completo, registro de beneficiarios y control de estatus en tiempo real.",
  },
  {
    icon: "💰",
    title: "Ahorros Automatizados",
    desc: "Registro de aportes, historial de movimientos y cálculo preciso de rendimientos sin intervención manual.",
  },
  {
    icon: "📄",
    title: "Documentación Legal Instantánea",
    desc: "Generación automática de contratos, pagarés y estados de cuenta en PDF con validez operativa.",
  },
  {
    icon: "📊",
    title: "Panel Administrativo",
    desc: "Dashboard con métricas clave, control de parámetros y supervisión operativa centralizada.",
  },
];

interface Phase {
  id: string;
  title: string;
  timeframe: string;
  budget: string;
  features: string[];
  status: "active" | "planned";
}

const PHASES: Phase[] = [
  {
    id: "mvp",
    title: "Fase 1: Sistema Base (MVP)",
    timeframe: "2–3 semanas",
    budget: "$400 USD",
    features: ["Gestión de socios y beneficiarios", "Ahorros y cálculo de intereses", "Generación masiva de PDFs", "Panel administrativo básico"],
    status: "active",
  },
  {
    id: "opt",
    title: "Fase 2: Optimización",
    timeframe: "4–6 semanas",
    budget: "Por definir",
    features: ["Módulo de créditos online", "Tablas de amortización dinámicas", "Motor de notificaciones (email/SMS)", "Reportes avanzados y exportables"],
    status: "planned",
  },
  {
    id: "scale",
    title: "Fase 3: Expansión",
    timeframe: "Q3–Q4 2026",
    budget: "Ronda semilla",
    features: ["Validación KYC (SAIME)", "WebAuthn / Biometría web", "Marketplace de aliados comerciales", "Escalado a 20k+ usuarios sin refactor"],
    status: "planned",
  },
];

const TIERS = [
  {
    name: "MVP Piloto",
    price: "$400 USD",
    period: "pago único",
    features: ["Sistema base funcional", "Entrega en 2–3 semanas", "Soporte inicial 30 días", "Documentación API completa"],
    cta: "Solicitar Demo",
    highlighted: false,
  },
  {
    name: "Licencia Institucional",
    price: "Personalizado",
    period: "mensual / anual",
    features: ["Arquitectura multi-tenant", "Portal socio + admin", "Generación ilimitada de PDFs", "SLA 99.9% y backups automáticos"],
    cta: "Hablar con Ventas",
    highlighted: true,
  },
  {
    name: "Inversión Estratégica",
    price: "Ronda Semilla",
    period: "equity / revenue share",
    features: ["Acceso a métricas en tiempo real", "Influencia directa en roadmap", "Expansión regional priorizada", "Reportes financieros auditables"],
    cta: "Descargar One-Pager",
    highlighted: false,
  },
];

const LETTER = {
  title: "Nuestro compromiso con la transparencia",
  body: `Construimos esta plataforma bajo la filosofía de "empezar ligero, validar rápido y escalar sin límites". Cada línea de código, cada cálculo financiero y cada documento generado cumple con estándares de auditoría inmutable y está alineado a la LOPDP y lineamientos SUDEBAN/SUDECA. No vendemos humo: entregamos infraestructura real para el ahorro colectivo en Venezuela y Latinoamérica.`,
  signature: "Equipo Fundador",
  role: "Fondo de Ahorro Platform",
};

// ==========================================
// COMPONENTES INTERNOS
// ==========================================
function SectionTitle({ children, className }: { children: React.ReactNode; className?: string }) {
  return (
    <h2 className={`text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl ${className || ""}`}>
      {children}
    </h2>
  );
}

function Badge({ children, variant = "default" }: { children: React.ReactNode; variant?: "default" | "active" | "planned" }) {
  const styles = {
    default: "bg-gray-100 text-gray-800",
    active: "bg-blue-100 text-blue-800",
    planned: "bg-amber-100 text-amber-800",
  };
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${styles[variant]}`}>
      {children}
    </span>
  );
}

function Hero() {
  return (
    <section className="relative w-full overflow-hidden bg-gradient-to-b from-slate-50 to-white px-4 py-20 sm:px-6 lg:px-8 lg:py-32">
      <div className="mx-auto max-w-4xl text-center">
        <Badge variant="active">v1.0.0 MVP Disponible</Badge>
        <h1 className="mt-6 text-4xl font-extrabold tracking-tight text-gray-900 sm:text-5xl lg:text-6xl">
          Digitaliza, Automatiza y <span className="text-blue-600">Escala</span> tu Fondo de Ahorro
        </h1>
        <p className="mt-6 text-lg leading-8 text-gray-600 max-w-2xl mx-auto">
          Plataforma web integral para gestionar socios, aportes y créditos con transparencia, cumplimiento normativo y arquitectura lista para +20,000 afiliados.
        </p>
        <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
          <a
            href="/registro"
            className="w-full sm:w-auto rounded-lg bg-blue-600 px-6 py-3 text-base font-semibold text-white shadow-sm hover:bg-blue-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 transition"
          >
            Crear Cuenta Gratuita
          </a>
          <a
            href="#inversion"
            className="w-full sm:w-auto rounded-lg bg-white px-6 py-3 text-base font-semibold text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 transition"
          >
            Ver Oportunidad de Inversión
          </a>
        </div>
        <div className="mt-10 text-sm text-gray-500">
          Stack: Next.js + Java 21 + Spring Boot + PostgreSQL • Cumple LOPDP / SUDEBAN
        </div>
      </div>
    </section>
  );
}

function Features() {
  return (
    <section className="px-4 py-16 sm:px-6 lg:px-8 bg-white">
      <div className="mx-auto max-w-7xl">
        <div className="text-center mb-12">
          <SectionTitle>Módulos Core del MVP</SectionTitle>
          <p className="mt-4 text-lg text-gray-600">Todo lo que necesitas para operar sin errores manuales ni papeleo.</p>
        </div>
        <div className="grid grid-cols-1 gap-8 md:grid-cols-2 lg:grid-cols-4">
          {FEATURES.map((f) => (
            <div key={f.title} className="group relative rounded-xl border border-gray-200 p-6 bg-white shadow-sm hover:shadow-md transition">
              <div className="text-3xl mb-4">{f.icon}</div>
              <h3 className="text-lg font-semibold text-gray-900">{f.title}</h3>
              <p className="mt-2 text-gray-600 text-sm leading-relaxed">{f.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function Roadmap() {
  return (
    <section id="roadmap" className="px-4 py-16 sm:px-6 lg:px-8 bg-slate-50">
      <div className="mx-auto max-w-7xl">
        <div className="text-center mb-12">
          <SectionTitle>Hoja de Ruta & Fases</SectionTitle>
          <p className="mt-4 text-lg text-gray-600">Crecimiento planificado, validación rápida y escalabilidad garantizada.</p>
        </div>
        <div className="space-y-6 lg:space-y-0 lg:grid lg:grid-cols-3 lg:gap-6">
          {PHASES.map((p) => (
            <div key={p.id} className="relative flex flex-col rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <Badge variant={p.status}>{p.status === "active" ? "En desarrollo" : "Planificado"}</Badge>
                <span className="text-xs font-medium text-gray-500">{p.timeframe}</span>
              </div>
              <h3 className="text-xl font-bold text-gray-900">{p.title}</h3>
              <p className="mt-2 text-sm font-semibold text-blue-600">{p.budget}</p>
              <ul className="mt-4 space-y-2">
                {p.features.map((feat) => (
                  <li key={feat} className="flex items-start text-sm text-gray-600">
                    <span className="mr-2 mt-0.5 h-1.5 w-1.5 rounded-full bg-blue-500" />
                    {feat}
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function InvestmentTiers() {
  return (
    <section id="inversion" className="px-4 py-16 sm:px-6 lg:px-8 bg-white">
      <div className="mx-auto max-w-7xl">
        <div className="text-center mb-12">
          <SectionTitle>Modelo de Acceso & Inversión</SectionTitle>
          <p className="mt-4 text-lg text-gray-600">Elige cómo quieres participar: como usuario institucional o como inversor estratégico.</p>
        </div>
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-3 lg:gap-6">
          {TIERS.map((t) => (
            <div
              key={t.name}
              className={`flex flex-col rounded-2xl border p-8 ${t.highlighted ? "border-blue-200 bg-blue-50 shadow-lg scale-100 lg:scale-105" : "border-gray-200 bg-white"
                }`}
            >
              <h3 className="text-xl font-bold text-gray-900">{t.name}</h3>
              <div className="mt-4 flex items-baseline gap-1">
                <span className="text-4xl font-bold tracking-tight text-gray-900">{t.price}</span>
                <span className="text-sm text-gray-500">/{t.period}</span>
              </div>
              <ul className="mt-6 space-y-3 flex-1">
                {t.features.map((feat) => (
                  <li key={feat} className="flex items-start text-sm text-gray-600">
                    <svg className="mr-2 mt-0.5 h-5 w-5 flex-none text-green-500" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clipRule="evenodd" />
                    </svg>
                    {feat}
                  </li>
                ))}
              </ul>
              <a
                href="#contacto"
                className={`mt-8 block w-full rounded-lg px-4 py-3 text-center text-sm font-semibold shadow-sm transition ${t.highlighted
                    ? "bg-blue-600 text-white hover:bg-blue-700"
                    : "bg-gray-900 text-white hover:bg-gray-800"
                  }`}
              >
                {t.cta}
              </a>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function TrustLetter() {
  return (
    <section id="contacto" className="px-4 py-16 sm:px-6 lg:px-8 bg-slate-900 text-white">
      <div className="mx-auto max-w-3xl text-center">
        <SectionTitle className="text-white">{LETTER.title}</SectionTitle>
        <p className="mt-6 text-lg leading-relaxed text-slate-300">{LETTER.body}</p>
        <div className="mt-8 flex flex-wrap items-center justify-center gap-3 text-sm font-medium text-slate-400">
          <span className="inline-flex items-center gap-1.5">🔒 JWT + Cifrado E2E</span>
          <span className="inline-flex items-center gap-1.5">📜 Logs Inalterables</span>
          <span className="inline-flex items-center gap-1.5">🇻🇪 LOPDP / SUDEBAN</span>
          <span className="inline-flex items-center gap-1.5">🧮 Decimal.js + Zod</span>
        </div>
        <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
          <a
            href="mailto:contacto@fondoahorro.dev"
            className="rounded-lg bg-blue-600 px-6 py-3 text-base font-semibold text-white shadow-sm hover:bg-blue-700 transition"
          >
            Contactar al Equipo
          </a>
          <a
            href="/docs/api"
            className="rounded-lg border border-slate-600 px-6 py-3 text-base font-semibold text-white hover:bg-slate-800 transition"
          >
            Documentación Técnica
          </a>
        </div>
        <div className="mt-8 text-sm text-slate-500">
          Firmado por <span className="font-medium text-slate-300">{LETTER.signature}</span> • {LETTER.role}
        </div>
      </div>
    </section>
  );
}

function Footer() {
  return (
    <footer className="border-t border-gray-200 bg-white px-4 py-8 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-7xl flex flex-col items-center justify-between gap-4 sm:flex-row">
        <p className="text-sm text-gray-500">© {new Date().getFullYear()} Fondo de Ahorro Platform. Todos los derechos reservados.</p>
        <nav className="flex gap-6 text-sm text-gray-500">
          <a href="/login" className="hover:text-gray-900 transition">Iniciar Sesión</a>
          <a href="/registro" className="hover:text-gray-900 transition">Registrarse</a>
          <a href="#roadmap" className="hover:text-gray-900 transition">Roadmap</a>
          <a href="#inversion" className="hover:text-gray-900 transition">Inversión</a>
        </nav>
      </div>
    </footer>
  );
}

// ==========================================
// COMPONENTE PRINCIPAL
// ==========================================
export default function Home() {
  return (
    <main className="min-h-screen bg-white text-gray-900 antialiased scroll-smooth">
      <Hero />
      <Features />
      <Roadmap />
      <InvestmentTiers />
      <TrustLetter />
      <Footer />
    </main>
  );
}