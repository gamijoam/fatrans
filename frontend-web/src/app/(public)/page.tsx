export const dynamic = "force-dynamic";

import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Fatrans | El respaldo financiero del transportista",
  description: "Ahorra en VES y USD, accede a crédito express y gestiona tu unidad. Solo con tu cédula.",
  openGraph: {
    title: "Fatrans | Finanzas para el Transporte",
    description: "Tu fondo de ahorro diseñado para choferes, propietarios y mecánicos.",
    type: "website",
  },
};

// ==========================================
// SVG ICONS - Clean vector icons
// ==========================================
function IconWallet({ className = "" }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 12V7H5a2 2 0 0 1 0-4h14v4" />
      <path d="M3 5v14a2 2 0 0 0 2 2h16v-5" />
      <path d="M18 12a2 2 0 0 0 0 4h4v-4Z" />
    </svg>
  );
}

function IconCredit({ className = "" }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <rect x="2" y="5" width="20" height="14" rx="2" />
      <path d="M2 10h20" />
      <path d="M6 15h4" />
    </svg>
  );
}

function IconShield({ className = "" }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
      <path d="m9 12 2 2 4-4" />
    </svg>
  );
}

function IconTruck({ className = "" }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M14 18V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v11a1 1 0 0 0 1 1h2" />
      <path d="M15 18H9" />
      <path d="M19 18h2a1 1 0 0 0 1-1v-3.65a1 1 0 0 0-.22-.624l-3.48-4.35A1 1 0 0 0 17.52 8H14" />
      <circle cx="17" cy="18" r="2" />
      <circle cx="7" cy="18" r="2" />
    </svg>
  );
}

function IconTrend({ className = "" }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="m22 7-8.5 8.5-5-5L2 17" />
      <path d="M16 7h6v6" />
    </svg>
  );
}

function IconPhone({ className = "" }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <rect x="5" y="2" width="14" height="20" rx="2" />
      <path d="M12 18h.01" />
    </svg>
  );
}

function IconCheck({ className = "" }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 6 9 17l-5-5" />
    </svg>
  );
}

// ==========================================
// DATA
// ==========================================
const BENEFITS = [
  {
    icon: IconWallet,
    title: "Ahorra en VES y USD",
    description: "Cuentas digitales en ambas monedas. Sin mínimos, sin comisiones ocultas. Tu dinero crece con tasas competitivas.",
  },
  {
    icon: IconCredit,
    title: "Crédito en 24 horas",
    description: "Solicita y recibe aprobación en un día. Colateral de tu propio ahorro. Sin paperwork innecesario.",
  },
  {
    icon: IconTruck,
    title: "Gestiona tu unidad",
    description: "Registro de vehículo, rutas y turnos. Todo relacionado a tu cuenta de ahorro y crédito.",
  },
  {
    icon: IconShield,
    title: "Seguro y protegido",
    description: "Tus datos y dinero están cifrados. Cumplimos con las normativas venezolanas de protección de datos.",
  },
];

const TRUST_POINTS = [
  "Tasa de cambio competitiva",
  "Atención por WhatsApp",
  "Cumple normativa LOPDP",
  "Soporte en español",
];

const TRANSPORT_LOGOS = [
  { name: "Línea Express", abbr: "LE" },
  { name: "TransVenezuela", abbr: "TV" },
  { name: "Coop. Carabobo", abbr: "CC" },
  { name: "RutaPlus", abbr: "RP" },
];

// ==========================================
// COMPONENTS
// ==========================================
function Navbar() {
  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-white/80 backdrop-blur-lg border-b border-slate-100">
      <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-[#16A34A] flex items-center justify-center">
            <IconTruck className="w-4 h-4 text-white" />
          </div>
          <span className="text-xl font-bold text-[#0F2744]">Fatrans</span>
        </div>

        <div className="hidden md:flex items-center gap-8">
          <a href="#beneficios" className="text-sm font-medium text-slate-600 hover:text-[#0F2744] transition">
            Beneficios
          </a>
          <a href="#como-funciona" className="text-sm font-medium text-slate-600 hover:text-[#0F2744] transition">
            Cómo funciona
          </a>
        </div>

        <div className="flex items-center gap-3">
          <a
            href={process.env.NEXT_PUBLIC_AUTH_URL ? `${process.env.NEXT_PUBLIC_AUTH_URL}/login` : "/login"}
            className="px-4 py-2 text-sm font-medium text-slate-700 hover:text-[#0F2744] transition"
          >
            Iniciar sesión
          </a>
          <a
            href={process.env.NEXT_PUBLIC_AUTH_URL ? `${process.env.NEXT_PUBLIC_AUTH_URL}/registro` : "/registro"}
            className="px-5 py-2.5 bg-[#16A34A] text-white text-sm font-semibold rounded-lg hover:bg-[#15803D] transition shadow-sm"
          >
            Abre tu cuenta
          </a>
        </div>
      </div>
    </nav>
  );
}

function Hero() {
  return (
    <section className="pt-32 pb-20 lg:pb-32 bg-gradient-to-b from-slate-50 to-white overflow-hidden">
      <div className="max-w-7xl mx-auto px-6">
        <div className="grid lg:grid-cols-2 gap-12 lg:gap-16 items-center">
          {/* Left Content */}
          <div className="space-y-8">
            <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-[#16A34A]/10 text-[#16A34A] text-sm font-medium">
              <span className="w-1.5 h-1.5 rounded-full bg-[#16A34A]" />
              Diseñado para transportistas
            </div>

            <h1 className="text-4xl lg:text-5xl xl:text-6xl font-bold text-[#0F2744] leading-tight">
              El respaldo financiero que tu{' '}
              <span className="text-[#16A34A]">unidad merece</span>
            </h1>

            <p className="text-lg text-slate-600 leading-relaxed max-w-lg">
              Ahorra en Bolívar y Dólar. Accede a crédito express. Gestiona tu vehículo. Todo en una sola plataforma diseñada para la realidad del transporte en Venezuela.
            </p>

            <div className="flex flex-col sm:flex-row gap-4">
              <a
                href={process.env.NEXT_PUBLIC_AUTH_URL ? `${process.env.NEXT_PUBLIC_AUTH_URL}/registro` : "/registro"}
                className="inline-flex items-center justify-center px-8 py-4 bg-[#16A34A] text-white font-semibold rounded-xl hover:bg-[#15803D] transition shadow-lg shadow-[#16A34A]/20"
              >
                Abre tu cuenta gratis
              </a>
              <a
                href="#beneficios"
                className="inline-flex items-center justify-center px-8 py-4 border border-slate-300 text-[#0F2744] font-semibold rounded-xl hover:bg-slate-50 transition"
              >
                Conoce más
              </a>
            </div>

            <div className="flex flex-wrap items-center gap-6 pt-4">
              {TRUST_POINTS.map((point) => (
                <div key={point} className="flex items-center gap-2 text-sm text-slate-500">
                  <IconCheck className="w-4 h-4 text-[#16A34A]" />
                  {point}
                </div>
              ))}
            </div>
          </div>

          {/* Right - App Mockup */}
          <div className="relative lg:h-[600px]">
            {/* Phone Frame */}
            <div className="relative mx-auto w-72 lg:w-80">
              <div className="absolute inset-0 bg-gradient-to-b from-[#0F2744] to-[#1a4a7a] rounded-[3rem] shadow-2xl" />
              <div className="absolute inset-1 bg-[#0F2744] rounded-[2.75rem] overflow-hidden">
                {/* Status Bar */}
                <div className="h-12 bg-[#0F2744] flex items-end justify-center pb-2">
                  <div className="w-20 h-6 bg-black rounded-full" />
                </div>
                {/* Screen Content */}
                <div className="bg-white rounded-b-[2.5rem] overflow-hidden">
                  <div className="p-5 space-y-4">
                    <div className="text-center">
                      <p className="text-xs text-slate-500 mb-1">Saldo disponible</p>
                      <p className="text-3xl font-bold text-[#0F2744]">Bs 125.450,00</p>
                      <p className="text-sm text-slate-500">≈ $ 2.450,00 USD</p>
                    </div>
                    <div className="flex gap-2">
                      <button className="flex-1 py-3 bg-[#16A34A] text-white text-sm font-semibold rounded-xl">
                        + Aportar
                      </button>
                      <button className="flex-1 py-3 bg-[#0F2744] text-white text-sm font-semibold rounded-xl">
                        Solicitar
                      </button>
                    </div>
                    <div className="space-y-2">
                      <div className="p-3 bg-slate-50 rounded-xl">
                        <p className="text-xs text-slate-500">Ahorro Programado</p>
                        <p className="text-lg font-semibold text-[#0F2744]">Bs 45.000,00</p>
                      </div>
                      <div className="p-3 bg-slate-50 rounded-xl">
                        <p className="text-xs text-slate-500">Crédito Disponible</p>
                        <p className="text-lg font-semibold text-[#16A34A]">Bs 80.000,00</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            {/* Decorative Elements */}
            <div className="absolute -top-8 -right-8 w-32 h-32 bg-[#16A34A]/10 rounded-full blur-3xl" />
            <div className="absolute -bottom-8 -left-8 w-40 h-40 bg-[#0F2744]/10 rounded-full blur-3xl" />
          </div>
        </div>
      </div>
    </section>
  );
}

function TrustBand() {
  return (
    <section className="py-10 bg-slate-100/50 border-y border-slate-200">
      <div className="max-w-7xl mx-auto px-6">
        <p className="text-center text-sm text-slate-500 mb-8">
          Diseñado para las principales líneas de transporte de Venezuela
        </p>
        <div className="flex flex-wrap justify-center items-center gap-8 lg:gap-16">
          {TRANSPORT_LOGOS.map((logo) => (
            <div
              key={logo.name}
              className="flex items-center gap-3 opacity-40 hover:opacity-70 transition-opacity"
            >
              <div className="w-10 h-10 rounded-lg bg-[#0F2744] flex items-center justify-center">
                <span className="text-white font-bold text-sm">{logo.abbr}</span>
              </div>
              <span className="text-sm font-medium text-slate-600">{logo.name}</span>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function Benefits() {
  return (
    <section id="beneficios" className="py-20 lg:py-32 bg-white">
      <div className="max-w-7xl mx-auto px-6">
        <div className="text-center max-w-2xl mx-auto mb-16">
          <h2 className="text-3xl lg:text-4xl font-bold text-[#0F2744]">
            Todo lo que necesitas en un solo lugar
          </h2>
          <p className="mt-4 text-lg text-slate-600">
            Herramientas financieras diseñadas específicamente para la realidad del transportista venezolano.
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
          {BENEFITS.map((benefit) => (
            <div
              key={benefit.title}
              className="group p-6 lg:p-8 bg-white border border-slate-200 rounded-2xl hover:border-[#16A34A]/30 hover:shadow-xl hover:shadow-[#16A34A]/5 transition-all duration-300"
            >
              <div className="w-12 h-12 rounded-xl bg-[#16A34A]/10 flex items-center justify-center mb-5 group-hover:bg-[#16A34A] group-hover:scale-110 transition-all duration-300">
                <benefit.icon className="w-6 h-6 text-[#16A34A] group-hover:text-white transition-colors" />
              </div>
              <h3 className="text-lg font-semibold text-[#0F2744] mb-2">
                {benefit.title}
              </h3>
              <p className="text-sm text-slate-600 leading-relaxed">
                {benefit.description}
              </p>
            </div>
          ))}
        </div>

        {/* Featured Balance Card */}
        <div className="mt-16 max-w-2xl mx-auto">
          <div className="p-8 lg:p-10 bg-gradient-to-br from-[#0F2744] via-[#0F2744] to-[#1a4a7a] rounded-3xl text-white shadow-2xl">
            <div className="flex items-center justify-between mb-6">
              <span className="text-sm font-medium text-white/70">Tu saldo disponible</span>
              <div className="flex items-center gap-2 px-3 py-1 bg-white/10 rounded-full">
                <IconTrend className="w-4 h-4 text-[#16A34A]" />
                <span className="text-xs font-medium">+2.5% este mes</span>
              </div>
            </div>

            <div className="space-y-2 mb-8">
              <p className="text-4xl lg:text-5xl font-bold">Bs 125.450,00</p>
              <p className="text-lg text-white/60">≈ $ 2.450,00 USD</p>
            </div>

            <div className="grid grid-cols-2 gap-4 mb-8">
              <div className="p-4 bg-white/10 rounded-xl">
                <p className="text-xs text-white/60 mb-1">Ahorro VES</p>
                <p className="text-xl font-bold">Bs 85.000</p>
              </div>
              <div className="p-4 bg-white/10 rounded-xl">
                <p className="text-xs text-white/60 mb-1">Ahorro USD</p>
                <p className="text-xl font-bold">$ 1.200</p>
              </div>
            </div>

            <a
              href={process.env.NEXT_PUBLIC_AUTH_URL ? `${process.env.NEXT_PUBLIC_AUTH_URL}/registro` : "/registro"}
              className="block w-full py-4 bg-[#16A34A] text-white font-semibold text-center rounded-xl hover:bg-[#15803D] transition"
            >
              Empieza a ahorrar
            </a>
          </div>
          <p className="mt-4 text-center text-sm text-slate-500">
            Vista previa de tu cuenta Fatrans
          </p>
        </div>
      </div>
    </section>
  );
}

function HowItWorks() {
  const steps = [
    {
      number: "01",
      title: "Regístrate con tu cédula",
      description: "Solo necesitas tu cédula de identidad y un número de teléfono. Sin papeleo, sin complicaciones.",
    },
    {
      number: "02",
      title: "Abre tu cuenta de ahorro",
      description: "Elige entre cuentas en VES o USD. Empieza a ahorrar desde el primer día sin montos mínimos.",
    },
    {
      number: "03",
      title: "Accede a crédito cuando lo necesites",
      description: "Usa tu ahorro como colateral y accede a crédito express en menos de 24 horas.",
    },
  ];

  return (
    <section id="como-funciona" className="py-20 lg:py-32 bg-slate-50">
      <div className="max-w-7xl mx-auto px-6">
        <div className="text-center max-w-2xl mx-auto mb-16">
          <h2 className="text-3xl lg:text-4xl font-bold text-[#0F2744]">
            Abre tu cuenta en minutos
          </h2>
          <p className="mt-4 text-lg text-slate-600">
            Tres pasos simples para empezar a ser parte del fondo de ahorro más grande del transporte.
          </p>
        </div>

        <div className="grid md:grid-cols-3 gap-8">
          {steps.map((step, index) => (
            <div key={step.number} className="relative">
              <div className="p-8 bg-white rounded-2xl border border-slate-200 h-full">
                <div className="text-6xl font-bold text-[#16A34A]/10 mb-4">
                  {step.number}
                </div>
                <h3 className="text-xl font-semibold text-[#0F2744] mb-3">
                  {step.title}
                </h3>
                <p className="text-slate-600 leading-relaxed">
                  {step.description}
                </p>
              </div>
              {index < steps.length - 1 && (
                <div className="hidden md:block absolute top-1/2 -right-4 transform -translate-y-1/2">
                  <div className="w-8 h-0.5 bg-slate-300" />
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function CTABand() {
  return (
    <section className="py-20 bg-[#16A34A]">
      <div className="max-w-4xl mx-auto px-6 text-center">
        <h2 className="text-3xl lg:text-4xl font-bold text-white mb-6">
          Abre tu cuenta en minutos, solo con tu cédula
        </h2>
        <p className="text-lg text-white/80 mb-10 max-w-xl mx-auto">
          Sin costos de apertura. Sin mantenimiento. Sin letra pequeña. Empieza a ahorrar hoy mismo.
        </p>
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <a
            href={process.env.NEXT_PUBLIC_AUTH_URL ? `${process.env.NEXT_PUBLIC_AUTH_URL}/registro` : "/registro"}
            className="inline-flex items-center justify-center px-10 py-4 bg-white text-[#16A34A] font-bold rounded-xl hover:bg-slate-100 transition shadow-lg"
          >
            Crear mi cuenta gratis
          </a>
          <a
            href={process.env.NEXT_PUBLIC_AUTH_URL ? `${process.env.NEXT_PUBLIC_AUTH_URL}/login` : "/login"}
            className="inline-flex items-center justify-center px-10 py-4 border-2 border-white text-white font-semibold rounded-xl hover:bg-white/10 transition"
          >
            Ya tengo cuenta
          </a>
        </div>
      </div>
    </section>
  );
}

function Footer() {
  return (
    <footer className="bg-[#0F2744] text-white py-16">
      <div className="max-w-7xl mx-auto px-6">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8 mb-12">
          <div className="col-span-2 md:col-span-1">
            <div className="flex items-center gap-2 mb-4">
              <div className="w-8 h-8 rounded-lg bg-[#16A34A] flex items-center justify-center">
                <IconTruck className="w-4 h-4 text-white" />
              </div>
              <span className="text-xl font-bold">Fatrans</span>
            </div>
            <p className="text-sm text-slate-400 leading-relaxed">
              Plataforma financiera para el sector transporte venezolano.
            </p>
          </div>

          <div>
            <h4 className="font-semibold mb-4">Plataforma</h4>
            <ul className="space-y-2 text-sm text-slate-400">
              <li><a href={process.env.NEXT_PUBLIC_AUTH_URL ? `${process.env.NEXT_PUBLIC_AUTH_URL}/registro` : "/registro"} className="hover:text-white transition">Registrarse</a></li>
              <li><a href={process.env.NEXT_PUBLIC_AUTH_URL ? `${process.env.NEXT_PUBLIC_AUTH_URL}/login` : "/login"} className="hover:text-white transition">Iniciar sesión</a></li>
              <li><a href="#beneficios" className="hover:text-white transition">Beneficios</a></li>
            </ul>
          </div>

          <div>
            <h4 className="font-semibold mb-4">Legal</h4>
            <ul className="space-y-2 text-sm text-slate-400">
              <li><a href="#" className="hover:text-white transition">Términos de Servicio</a></li>
              <li><a href="#" className="hover:text-white transition">Política de Privacidad</a></li>
              <li><a href="#" className="hover:text-white transition">Cumplimiento LOPDP</a></li>
            </ul>
          </div>

          <div>
            <h4 className="font-semibold mb-4">Contacto</h4>
            <ul className="space-y-2 text-sm text-slate-400">
              <li><a href="mailto:soporte@fatans.com" className="hover:text-white transition">soporte@fatans.com</a></li>
              <li><a href="https://wa.me/5800000000000" className="hover:text-white transition">WhatsApp</a></li>
            </ul>
          </div>
        </div>

        <div className="pt-8 border-t border-slate-800 flex flex-col md:flex-row justify-between items-center gap-4 text-sm text-slate-500">
          <p>© {new Date().getFullYear()} Fatrans. Todos los derechos reservados.</p>
          <p>Plataforma alineada a LOPDP y normativas venezolanas</p>
        </div>
      </div>
    </footer>
  );
}

// ==========================================
// MAIN EXPORT
// ==========================================
export default function Home() {
  return (
    <main className="min-h-screen bg-white antialiased">
      <Navbar />
      <Hero />
      <TrustBand />
      <Benefits />
      <HowItWorks />
      <CTABand />
      <Footer />
    </main>
  );
}
