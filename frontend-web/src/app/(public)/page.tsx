import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Fatrans | El respaldo que los bancos no te dan",
  description:
    "Plataforma financiera para transportistas venezolanos. Ahorro en VES/USD, crédito express 24h, gestión de unidades y protección colectiva. Cumple LOPDP/SUDEBAN.",
  openGraph: {
    title: "Fatrans | Finanzas para el Transporte Venezolano",
    description: "Ahorro, crédito y gestión diseñados para choferes, propietarios y mecánicos.",
    type: "website",
  },
};

// ==========================================
// DATOS ESTÁTICOS - PLAN MAESTRO FATRANS v2026
// ==========================================
const PILLARS = [
  {
    icon: "verified_user",
    title: "Identidad y Confianza",
    desc: "KYC con cédula/RIF/licencia, aval grupal y score crediticio alternativo basado en tu historial de recaudo.",
  },
  {
    icon: "savings",
    title: "Ahorro Adaptado",
    desc: "Cuentas en VES y USD, microahorro automático (% del recaudo) y metas programadas como 'Nuevos Cauchos'.",
  },
  {
    icon: "credit_card",
    title: "Crédito Express",
    desc: "Aprobación en 24h para repuestos, SOAT o financiamiento de unidades. Sin burocracia, con colateral de ahorro.",
  },
  {
    icon: "local_shipping",
    title: "Gestión de Transporte",
    desc: "Perfil de tu unidad (placa/marca/año), registro de rutas, turnos y recaudos diarios en un solo lugar.",
  },
  {
    icon: "shield",
    title: "Protección y Bienestar",
    desc: "Beneficiarios, fondo de solidaridad grupal y seguros colectivos diseñados para el gremio transportista.",
  },
  {
    icon: "admin_panel_settings",
    title: "Administración y Control",
    desc: "Dashboard de KPIs, gestión de riesgos, auditoría inmutable y configuración de parámetros operativos.",
  },
];

const DIFFERENTIATORS = [
  {
    title: "Crédito de Emergencia 24h",
    desc: "Aprobación automática basada en tu saldo de ahorro y score interno. Dinero disponible cuando más lo necesitas.",
    icon: "bolt",
  },
  {
    title: "Ahorro por Meta Visual",
    desc: "Ve tu progreso hacia objetivos reales: 'Motor Nuevo', 'Viaje Familiar', 'Fondo de Emergencia'.",
    icon: "target",
  },
  {
    title: "Perfil de Unidad Vinculado",
    desc: "Asocia tu vehículo a tu cuenta para recibir ofertas de repuestos, seguros y mantenimiento personalizados.",
    icon: "directions_car",
  },
  {
    title: "Notificaciones por WhatsApp",
    desc: "Alertas críticas de seguridad y movimientos directamente a tu celular. El canal #1 en Venezuela.",
    icon: "message",
  },
];

const ROADMAP = [
  {
    phase: "Fase 1: MVP",
    timeframe: "Q2 2026",
    status: "En desarrollo",
    items: ["Onboarding progresivo (5 pasos)", "Ahorro básico VES/USD", "KYC con cédula/RIF", "Dashboard socio esencial"],
  },
  {
    phase: "Fase 2: Diferenciadores",
    timeframe: "Q3 2026",
    status: "Planificado",
    items: ["Crédito Express 24h", "Ahorro por metas visuales", "Gestión de unidades y rutas", "Integración WhatsApp básica"],
  },
  {
    phase: "Fase 3: Escala",
    timeframe: "Q4 2026",
    status: "Planificado",
    items: ["App móvil Flutter v1", "Módulo de solidaridad grupal", "Marketplace de aliados (repuestos, talleres)", "Escalado a 20k+ usuarios"],
  },
];

const SECURITY = [
  { layer: "Autenticación", protocol: "JWT HS384 + Refresh Tokens httpOnly", status: "ACTIVO" },
  { layer: "Cifrado", protocol: "AES-256 en reposo y TLS 1.3 en tránsito", status: "ACTIVO" },
  { layer: "Auditoría", protocol: "Logs inalterables: quién, qué, cuándo, IP, dispositivo", status: "ACTIVO" },
  { layer: "Anti-Fraude", protocol: "Límites geográficos, bloqueo tras 5 intentos, PIN de pánico", status: "ACTIVO" },
  { layer: "Cumplimiento", protocol: "LOPDP + Lineamientos SUDEBAN/SUDECA", status: "ACTIVO" },
];

// ==========================================
// UTILIDADES UI
// ==========================================
function Badge({ children, variant = "default" }: { children: React.ReactNode; variant?: "default" | "success" | "warning" }) {
  const styles = {
    default: "bg-slate-100 text-slate-800",
    success: "bg-emerald-100 text-emerald-800",
    warning: "bg-amber-100 text-amber-800",
  };
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold ${styles[variant]}`}>
      {children}
    </span>
  );
}

function SectionTitle({ children, className = "" }: { children: React.ReactNode; className?: string }) {
  return (
    <h2 className={`text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl ${className}`}>
      {children}
    </h2>
  );
}

function Icon({ name, className = "" }: { name: string; className?: string }) {
  const icons: Record<string, string> = {
    verified_user: "🪪",
    savings: "💰",
    credit_card: "💳",
    local_shipping: "🚚",
    shield: "🛡️",
    admin_panel_settings: "⚙️",
    bolt: "⚡",
    target: "🎯",
    directions_car: "🚗",
    message: "💬",
    check_circle: "✅",
    account_balance: "🏦",
  };
  return <span className={`text-3xl ${className}`}>{icons[name] || "✨"}</span>;
}

// ==========================================
// COMPONENTES DE SECCIÓN
// ==========================================
function Navbar() {
  return (
    <nav className="bg-[#1A3C6E] border-b border-slate-700 sticky top-0 z-50">
      <div className="flex justify-between items-center w-full px-6 py-4 max-w-7xl mx-auto">
        <div className="text-2xl font-black text-white tracking-tighter flex items-center gap-2">
          <span className="text-[#2E7D32]">●</span> Fatrans
        </div>
        <div className="hidden md:flex items-center gap-6">
          <a className="text-sm font-medium text-white border-b-2 border-[#2E7D32] pb-1" href="#pilares">Pilares</a>
          <a className="text-sm font-medium text-slate-300 hover:text-white transition" href="#diferenciadores">Ventajas</a>
          <a className="text-sm font-medium text-slate-300 hover:text-white transition" href="#roadmap">Roadmap</a>
          <a className="text-sm font-medium text-slate-300 hover:text-white transition" href="#seguridad">Seguridad</a>
        </div>
        <div className="flex items-center gap-3">
          <a href="/login" className="px-4 py-2 text-sm font-medium text-white hover:bg-white/10 transition rounded-lg">
            Tu Cartera
          </a>
          <a href="/registro" className="px-5 py-2 bg-[#2E7D32] text-white rounded-lg text-sm font-semibold hover:bg-[#256528] transition shadow-sm">
            Afiliarse
          </a>
        </div>
      </div>
    </nav>
  );
}

function Hero() {
  return (
    <section className="relative bg-[#1A3C6E] overflow-hidden min-h-[600px] flex items-center">
      <div className="absolute inset-0 z-0">
        <div className="absolute inset-0 bg-gradient-to-br from-[#1A3C6E] via-[#1A3C6E]/95 to-[#2E7D32]/20"></div>
      </div>
      <div className="relative z-10 max-w-7xl mx-auto px-6 w-full">
        <div className="max-w-2xl">
          <Badge variant="success">v1.0.0 MVP • Sector Transporte</Badge>
          <h1 className="mt-4 text-4xl font-extrabold text-white sm:text-5xl lg:text-6xl leading-tight">
            El respaldo que los <span className="text-[#2E7D32]">bancos no te dan</span>
          </h1>
          <p className="mt-6 text-lg text-slate-200 max-w-lg">
            Plataforma financiera diseñada para choferes, propietarios y mecánicos venezolanos. Ahorro en VES/USD, crédito express 24h y gestión de tu unidad, todo en un solo lugar.
          </p>
          <div className="mt-10 flex flex-wrap gap-4">
            <a href="/registro" className="px-8 py-4 bg-[#2E7D32] text-white font-semibold rounded-lg hover:bg-[#256528] transition shadow-lg">
              Ir a Afiliarse
            </a>
            <a href="#inversion" className="px-8 py-4 border border-white/30 text-white font-semibold rounded-lg hover:bg-white/10 transition">
              Oportunidad de Inversión
            </a>
          </div>
          <div className="mt-8 flex items-center gap-4 text-sm text-slate-300">
            <span className="flex items-center gap-1">✅ Tasa BCV en tiempo real</span>
            <span className="flex items-center gap-1">✅ WhatsApp integrado</span>
            <span className="flex items-center gap-1">✅ Cumple LOPDP</span>
          </div>
        </div>
      </div>
    </section>
  );
}

function Pillars() {
  return (
    <section id="pilares" className="py-20 bg-slate-50 px-6">
      <div className="max-w-7xl mx-auto">
        <div className="mb-16 text-center max-w-3xl mx-auto">
          <SectionTitle>Los 6 Pilares de Fatrans</SectionTitle>
          <p className="mt-4 text-lg text-slate-600">
            Una estructura sólida que cubre cada necesidad financiera del transportista venezolano.
          </p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {PILLARS.map((p) => (
            <div key={p.title} className="p-6 bg-white border border-slate-200 rounded-xl hover:border-[#2E7D32] hover:shadow-md transition group">
              <Icon name={p.icon} className="text-[#1A3C6E] mb-4 group-hover:scale-110 transition-transform" />
              <h3 className="text-lg font-semibold text-slate-900 mb-2">{p.title}</h3>
              <p className="text-sm text-slate-600">{p.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function Differentiators() {
  return (
    <section id="diferenciadores" className="py-20 bg-white px-6">
      <div className="max-w-7xl mx-auto">
        <div className="text-center mb-16">
          <SectionTitle>¿Por qué Fatrans es diferente?</SectionTitle>
          <p className="mt-4 text-lg text-slate-600">
            No somos un banco tradicional. Entendemos el recaudo diario, la urgencia de repuestos y la realidad del transporte en Venezuela.
          </p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {DIFFERENTIATORS.map((d) => (
            <div key={d.title} className="p-6 bg-[#1A3C6E]/5 border border-[#1A3C6E]/10 rounded-xl">
              <Icon name={d.icon} className="text-[#2E7D32] mb-3" />
              <h4 className="font-semibold text-slate-900 mb-2">{d.title}</h4>
              <p className="text-sm text-slate-600">{d.desc}</p>
            </div>
          ))}
        </div>
        {/* Account Card Preview */}
        <div className="mt-16 max-w-2xl mx-auto">
          <div className="p-6 bg-gradient-to-br from-[#1A3C6E] to-[#254a8a] rounded-2xl text-white shadow-xl">
            <div className="flex items-center justify-between mb-4">
              <span className="text-sm font-medium opacity-90">Saldo Disponible</span>
              <Badge variant="success">Tasa BCV: 36.45 VES/USD</Badge>
            </div>
            <div className="text-4xl font-bold mb-1">12.450,00 VES</div>
            <div className="text-sm opacity-80">≈ $341,50 USD</div>
            <div className="mt-6 flex gap-3">
              <button className="flex-1 px-4 py-3 bg-[#2E7D32] rounded-lg font-medium hover:bg-[#256528] transition">
                + Aportar
              </button>
              <button className="flex-1 px-4 py-3 bg-white/10 rounded-lg font-medium hover:bg-white/20 transition">
                Solicitar Crédito
              </button>
            </div>
          </div>
          <p className="mt-4 text-center text-sm text-slate-500">
            Vista previa de tu Cartera Fatrans • Saldo VES grande + equivalente USD pequeño
          </p>
        </div>
      </div>
    </section>
  );
}

function RoadmapSection() {
  return (
    <section id="roadmap" className="py-20 bg-slate-50 px-6">
      <div className="max-w-7xl mx-auto">
        <div className="text-center mb-16">
          <SectionTitle>Roadmap Estratégico 2026</SectionTitle>
          <p className="mt-4 text-lg text-slate-600">
            Crecimiento planificado, validación rápida y escalabilidad garantizada para el sector transporte.
          </p>
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {ROADMAP.map((r) => (
            <div key={r.phase} className="p-6 bg-white border border-slate-200 rounded-xl">
              <div className="flex items-center justify-between mb-4">
                <Badge variant={r.status === "En desarrollo" ? "success" : "warning"}>{r.status}</Badge>
                <span className="text-xs text-slate-500 font-medium">{r.timeframe}</span>
              </div>
              <h3 className="font-semibold text-slate-900 mb-3">{r.phase}</h3>
              <ul className="space-y-2">
                {r.items.map((item) => (
                  <li key={item} className="flex items-start text-sm text-slate-600">
                    <span className="mr-2 mt-1 text-[#2E7D32]">●</span>
                    {item}
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

function SecuritySection() {
  return (
    <section id="seguridad" className="py-20 bg-white px-6">
      <div className="max-w-7xl mx-auto">
        <div className="flex flex-col md:flex-row justify-between items-end gap-6 mb-12">
          <div className="max-w-xl">
            <SectionTitle>Seguridad de Nivel Bancario</SectionTitle>
            <p className="mt-4 text-slate-600">
              Tu dinero y datos están protegidos con protocolos enterprise. Cada acción queda registrada de forma inmutable para tu tranquilidad.
            </p>
          </div>
          <div className="flex gap-6">
            <div className="text-right">
              <p className="text-xl font-bold text-[#1A3C6E]">JWT HS384</p>
              <p className="text-xs text-slate-500">Tokens seguros</p>
            </div>
            <div className="w-px h-12 bg-slate-300"></div>
            <div className="text-right">
              <p className="text-xl font-bold text-[#1A3C6E]">Auditoría 24/7</p>
              <p className="text-xs text-slate-500">Logs inalterables</p>
            </div>
          </div>
        </div>
        <div className="overflow-hidden border border-slate-200 rounded-xl bg-white">
          <table className="w-full text-left">
            <thead className="bg-[#1A3C6E] text-white">
              <tr>
                <th className="px-6 py-4 font-semibold">Capa de Protección</th>
                <th className="px-6 py-4 font-semibold">Protocolo</th>
                <th className="px-6 py-4 font-semibold">Estado</th>
              </tr>
            </thead>
            <tbody className="text-slate-700">
              {SECURITY.map((row, idx) => (
                <tr key={row.layer} className={idx % 2 === 0 ? "bg-white" : "bg-slate-50"}>
                  <td className="px-6 py-4 font-medium">{row.layer}</td>
                  <td className="px-6 py-4">{row.protocol}</td>
                  <td className="px-6 py-4">
                    <span className="px-2 py-1 bg-[#2E7D32]/10 text-[#2E7D32] text-xs font-semibold rounded-full">
                      {row.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
}

function InvestmentCTA() {
  return (
    <section id="inversion" className="py-20 bg-[#1A3C6E] px-6">
      <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
        <div className="text-white">
          <Badge variant="success">Oportunidad para Inversores</Badge>
          <h2 className="mt-4 text-3xl font-bold sm:text-4xl">
            Invierte en la infraestructura financiera del transporte venezolano
          </h2>
          <p className="mt-6 text-lg text-slate-200">
            Modelo SaaS web-first con bajo costo operativo, alta retención y expansión regional lista. Mercado objetivo: cooperativas de transporte, fondos de ahorro para choferes y asociaciones civiles en Venezuela y Latam.
          </p>
          <ul className="mt-8 space-y-3">
            <li className="flex items-center gap-2">
              <span className="text-[#2E7D32]">✅</span>
              <span className="text-slate-200">Acceso a métricas en tiempo real del sector</span>
            </li>
            <li className="flex items-center gap-2">
              <span className="text-[#2E7D32]">✅</span>
              <span className="text-slate-200">Influencia directa en el roadmap de producto</span>
            </li>
            <li className="flex items-center gap-2">
              <span className="text-[#2E7D32]">✅</span>
              <span className="text-slate-200">Reportes financieros auditables y transparentes</span>
            </li>
          </ul>
          <div className="mt-10 flex flex-wrap gap-4">
            <a href="mailto:inversion@fatrans.dev" className="px-8 py-4 bg-[#2E7D32] text-white font-semibold rounded-lg hover:bg-[#256528] transition">
              Solicitar Deck de Inversión
            </a>
            <a href="#contacto" className="px-8 py-4 border border-white/30 text-white font-semibold rounded-lg hover:bg-white/10 transition">
              Agendar Reunión
            </a>
          </div>
          <p className="mt-4 text-sm text-slate-400">Respuesta en &lt;24h • Información bajo NDA disponible</p>
        </div>
        {/* Stats Card */}
        <div className="bg-white/10 backdrop-blur-sm rounded-2xl p-8 border border-white/20">
          <h3 className="text-xl font-bold text-white mb-6">Métricas Clave del Proyecto</h3>
          <div className="grid grid-cols-2 gap-4">
            <div className="p-4 bg-white/5 rounded-lg">
              <p className="text-3xl font-bold text-[#2E7D32]">$400</p>
              <p className="text-xs text-slate-300">Inversión MVP inicial</p>
            </div>
            <div className="p-4 bg-white/5 rounded-lg">
              <p className="text-3xl font-bold text-[#2E7D32]">2-3 sem</p>
              <p className="text-xs text-slate-300">Tiempo de entrega MVP</p>
            </div>
            <div className="p-4 bg-white/5 rounded-lg">
              <p className="text-3xl font-bold text-[#2E7D32]">20k+</p>
              <p className="text-xs text-slate-300">Usuarios escalables</p>
            </div>
            <div className="p-4 bg-white/5 rounded-lg">
              <p className="text-3xl font-bold text-[#2E7D32]">99.9%</p>
              <p className="text-xs text-slate-300">SLA objetivo</p>
            </div>
          </div>
          <div className="mt-6 pt-6 border-t border-white/20">
            <p className="text-sm text-slate-300">
              <span className="font-semibold text-white">Stack:</span> Next.js + Spring Boot + PostgreSQL + Redis + Flutter
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}

function Footer() {
  return (
    <footer id="contacto" className="bg-slate-900 text-slate-300 px-6 py-12">
      <div className="max-w-7xl mx-auto">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
          <div className="md:col-span-2">
            <div className="text-xl font-black text-white flex items-center gap-2 mb-4">
              <span className="text-[#2E7D32]">●</span> Fatrans
            </div>
            <p className="text-sm text-slate-400 max-w-md">
              Plataforma financiera para el sector transporte venezolano. Ahorro, crédito y gestión diseñados para choferes, propietarios y mecánicos.
            </p>
          </div>
          <div>
            <h4 className="font-semibold text-white mb-3">Plataforma</h4>
            <ul className="space-y-2 text-sm">
              <li><a href="/registro" className="hover:text-[#2E7D32] transition">Registrarse</a></li>
              <li><a href="/login" className="hover:text-[#2E7D32] transition">Tu Cartera</a></li>
              <li><a href="#pilares" className="hover:text-[#2E7D32] transition">Módulos</a></li>
              <li><a href="#seguridad" className="hover:text-[#2E7D32] transition">Seguridad</a></li>
            </ul>
          </div>
          <div>
            <h4 className="font-semibold text-white mb-3">Legal</h4>
            <ul className="space-y-2 text-sm">
              <li><a href="#" className="hover:text-[#2E7D32] transition">Términos de Servicio</a></li>
              <li><a href="#" className="hover:text-[#2E7D32] transition">Política de Privacidad</a></li>
              <li><a href="#" className="hover:text-[#2E7D32] transition">Cumplimiento LOPDP</a></li>
              <li><a href="mailto:contacto@fatrans.dev" className="hover:text-[#2E7D32] transition">Contacto</a></li>
            </ul>
          </div>
        </div>
        <div className="pt-8 border-t border-slate-800 flex flex-col md:flex-row justify-between items-center gap-4 text-xs text-slate-500">
          <p>© {new Date().getFullYear()} Fatrans. Todos los derechos reservados.</p>
          <p>Plataforma alineada a LOPDP y lineamientos SUDEBAN/SUDECA</p>
        </div>
      </div>
    </footer>
  );
}

// ==========================================
// COMPONENTE PRINCIPAL
// ==========================================
export default function Home() {
  return (
    <main className="min-h-screen bg-white text-slate-900 antialiased scroll-smooth">
      <Navbar />
      <Hero />
      <Pillars />
      <Differentiators />
      <RoadmapSection />
      <SecuritySection />
      <InvestmentCTA />
      <Footer />
    </main>
  );
}