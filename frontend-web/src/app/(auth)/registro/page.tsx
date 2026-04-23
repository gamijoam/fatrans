import { RegistrationForm } from '@/components/features/auth/registration-form';

export default function RegistroPage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24 bg-gradient-to-br from-white via-green-50 to-blue-50">
      <div className="w-full max-w-md">
        <RegistrationForm />
      </div>
    </main>
  );
}