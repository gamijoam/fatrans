import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

export type UserRol = 'ADMIN' | 'ADMINISTRADOR' | 'GESTOR' | 'SOCIO' | 'SUPER_ADMIN';

export interface User {
  id: string;
  nombreUsuario: string;
  correoElectronico: string;
  nombreCompleto: string;
  rol: UserRol;
  socioId?: string;
  debeCambiarPassword?: boolean;
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setUser: (user: User | null) => void;
  setLoading: (loading: boolean) => void;
  logout: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  devtools(
    (set) => ({
      user: null,
      isAuthenticated: false,
      isLoading: true,
      setUser: (user) => set({ user, isAuthenticated: !!user }),
      setLoading: (isLoading) => set({ isLoading }),
      logout: async () => {
        const response = await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
        if (!response.ok) {
          throw new Error('Logout failed');
        }
        set({ user: null, isAuthenticated: false });
      },
    }),
    { name: 'AuthStore' }
  )
);
