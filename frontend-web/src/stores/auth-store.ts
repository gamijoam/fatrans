import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { devtools } from 'zustand/middleware';

export type UserRol = 'ADMIN' | 'ADMINISTRADOR' | 'GESTOR' | 'SOCIO';

export interface User {
  id: string;
  nombreUsuario: string;
  correoElectronico: string;
  nombreCompleto: string;
  rol: UserRol;
  socioId?: string;
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
    persist(
      (set) => ({
        user: null,
        isAuthenticated: false,
        isLoading: true,
        setUser: (user) => set({ user, isAuthenticated: !!user }),
        setLoading: (isLoading) => set({ isLoading }),
        logout: async () => {
          try {
            await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
          } finally {
            set({ user: null, isAuthenticated: false });
          }
        },
      }),
      { name: 'auth-storage' }
    ),
    { name: 'AuthStore' }
  )
);
