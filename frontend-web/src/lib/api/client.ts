import axios from 'axios';
import { toast } from 'sonner';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:18080/api';

export const apiClient = axios.create({
  baseURL: API_URL,
  timeout: 10_000,
  withCredentials: true,
});

apiClient.interceptors.request.use((config) => {
  const method = config.method?.toLowerCase();
  if (['post', 'put', 'delete', 'patch'].includes(method || '')) {
    const csrfToken = typeof document !== 'undefined' ? document.cookie
      .split('; ')
      .find((row) => row.startsWith('csrf_token='))
      ?.split('=')[1] : null;
    
    if (csrfToken) {
      config.headers['X-CSRF-Token'] = csrfToken;
    }
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        await apiClient.post('/v1/auth/refresh-web');
        return apiClient(originalRequest);
      } catch {
        toast.error('Sesión expirada. Por favor inicie sesión nuevamente.');
        if (typeof window !== 'undefined') {
          window.location.href = '/login';
        }
      }
    }

    const message = error.response?.data?.message || error.message || 'Error desconocido';
    // No mostrar toast para errores 404 de recursos no encontrados en dashboard (silencioso)
    if (error.response?.status !== 404) {
      toast.error(message);
    }
    
    return Promise.reject(error);
  }
);

export const authApi = {
  login: (identificador: string, password: string) =>
    apiClient.post('/v1/auth/login-web', { identificador, password }),
  logout: () => apiClient.post('/v1/auth/logout-web'),
  refresh: () => apiClient.post('/v1/auth/refresh-web'),
  me: () => apiClient.get('/v1/auth/me'),
  changePassword: (passwordActual: string, nuevoPassword: string) =>
    apiClient.post('/v1/auth/cambiar-password', { passwordActual, nuevoPassword }),
};

export const sociosApi = {
  getAll: () => apiClient.get('/v1/socios'),
  getById: (id: string) => apiClient.get(`/v1/socios/${id}`),
  updateProfile: (id: string, data: unknown) => apiClient.put(`/v1/socios/${id}/perfil`, data),
};

export const cuentasApi = {
  getCuentas: (socioId: string) => apiClient.get(`/v1/cuentas/socio/${socioId}`),
  getCuenta: (numeroCuenta: string) => apiClient.get(`/v1/cuentas/${numeroCuenta}`),
  getSaldo: (numeroCuenta: string) => apiClient.get(`/v1/cuentas/${numeroCuenta}/saldo`),
  getMovimientos: (numeroCuenta: string, page = 0, size = 10) => 
    apiClient.get(`/v1/cuentas/${numeroCuenta}/movimientos?page=${page}&size=${size}`),
  deposito: (numeroCuenta: string, monto: number) =>
    apiClient.post(`/v1/cuentas/${numeroCuenta}/depositos`, {
      monto,
      canalOrigen: 'WEB',
      descripcion: 'Depósito en línea'
    }),
  retiro: (numeroCuenta: string, monto: number) =>
    apiClient.post(`/v1/cuentas/${numeroCuenta}/retiros`, {
      monto,
      canalOrigen: 'WEB',
      descripcion: 'Retiro en línea'
    }),
};

export const creditosApi = {
  getTiposCredito: () => apiClient.get('/v1/creditos/tipos-credito'),
  getSolicitudesAdmin: (params?: Record<string, string>) => {
    const query = params ? new URLSearchParams(params).toString() : '';
    return apiClient.get(`/v1/admin/creditos/solicitudes${query ? `?${query}` : ''}`);
  },
  getSolicitudesPorSocio: (socioId: string) => apiClient.get(`/v1/creditos/solicitudes/socio/${socioId}`),
  simular: (data: unknown) => apiClient.post('/v1/simulador', data),
};

export const adminApi = {
  getStats: () => apiClient.get('/v1/admin/dashboard/estadisticas'),
  // Endpoint de actividad no existe en el backend, devolvemos array vacío para evitar 404/500
  getActividad: () => Promise.resolve({ data: [] }),
};
