import { SocioShell } from '@/components/layouts/socio/socio-shell';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <SocioShell>{children}</SocioShell>;
}
