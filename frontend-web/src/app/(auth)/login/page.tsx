import { LoginForm } from '@/components/features/auth/login-form';

export default function LoginPage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24 bg-gradient-to-br from-white via-green-50 to-blue-50">
      <div className="w-full max-w-md">
        <LoginForm />
      </div>
    </main>
  );
}