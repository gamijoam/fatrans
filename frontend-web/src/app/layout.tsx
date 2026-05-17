import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';
import { Providers } from './providers';
import { CookieConsentBanner } from '@/components/legal/cookie-consent-banner';

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'FATTRANS - Fondo de Ahorro',
  description: 'Plataforma digital de gestión de fondo de ahorro',
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