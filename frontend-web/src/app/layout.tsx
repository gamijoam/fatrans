import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';
import { Providers } from './providers';
import { CookieConsentBanner } from '@/components/legal/cookie-consent-banner';

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  // Plantilla: cada page puede definir `title: "X"` y se renderiza como
  // "X · Fatrans". Si una page no define title, queda el default.
  title: {
    default: 'Fatrans · Asociación de Ahorro y Crédito',
    template: '%s · Fatrans',
  },
  description:
    'Asociación de Ahorro y Crédito Fatrans (RIF J-50516835-5). Plataforma digital para socios del sector transporte: ahorro, créditos y servicios financieros en Venezuela.',
  applicationName: 'Fatrans',
  // Iconos servidos desde frontend-web/src/app/ (Next.js 14 los detecta
  // automáticamente: icon.png → favicon, apple-icon.png → iOS).
  // Fallback explícito a /logo-fatrans.png por si el navegador no lee
  // los conventional icons.
  icons: {
    icon: [
      { url: '/logo-fatrans.png', type: 'image/png' },
    ],
    apple: [
      { url: '/logo-fatrans.png', type: 'image/png' },
    ],
    shortcut: '/logo-fatrans.png',
  },
  openGraph: {
    title: 'Fatrans · Asociación de Ahorro y Crédito',
    description: 'Plataforma digital para socios del sector transporte venezolano.',
    siteName: 'Fatrans',
    type: 'website',
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="es">
      <body className={inter.className}>
        <Providers>{children}</Providers>
        {/* Banner de consentimiento de cookies (issue #218 PR-A).
            Aparece solo si no hay preferencia guardada en localStorage. */}
        <CookieConsentBanner />
      </body>
    </html>
  );
}