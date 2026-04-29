import type { Metadata } from "next";
import { LoginFormNeobank } from '@/components/features/auth/login-form-neobank';

export const metadata: Metadata = {
  title: "Iniciar Sesión | Fatrans",
  description: "Acceso seguro a tu cuenta Fatrans.",
};

export default function LoginPage() {
  return <LoginFormNeobank />;
}
