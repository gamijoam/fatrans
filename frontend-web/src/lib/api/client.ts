import axios from 'axios';
import { toast } from 'sonner';

const API_URL = process.env.NEXT_PUBLIC_API_URL || '';

export const apiClient = axios.create({
  baseURL: '',
  timeout: 10_000,
  withCredentials: true,
});

apiClient.interceptors.request.use((config) => {
  const method = config.method?.toLowerCase();
  if (['post', 'put', 'delete', 'patch'].includes(method || '')) {
    // CSRF: El backend debe enviar cookie 'csrf_token' con SameSite=Strict y HttpOnly=false
    // para que JavaScript pueda leerla. Alternativamente usar double-submit pattern.
    const csrfToken = document.cookie
      .split('; ')
      .find((row) => row.startsWith('csrf_token='))
      ?.split('=')[1];
    
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
        window.location.href = '/login';
      }
    }

    const message = error.response?.data?.message || error.message || 'Error desconocido';
    toast.error(message);
    
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
  getById: (id: string) => apiClient.get(`/v1/socios/${id}`),
  updateProfile: (id: string, data: unknown) => apiClient.put(`/v1/socios/${id}/perfil`, data),
};

export const cuentasApi = {
  getCuentas: (socioId: string) => apiClient.get(`/api/cuentas/socio/${socioId}`),
  getCuenta: (numeroCuenta: string) => apiClient.get(`/api/cuentas/${numeroCuenta}`),
  getSaldo: (numeroCuenta: string) => apiClient.get(`/api/cuentas/${numeroCuenta}/saldo`),
  getMovimientos: (numeroCuenta: string, page = 0, size = 10, fechaInicio?: string, fechaFin?: string, tipo?: string) => {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (fechaInicio) params.append('fechaInicio', fechaInicio);
    if (fechaFin) params.append('fechaFin', fechaFin);
    if (tipo) params.append('tipo', tipo);
    return apiClient.get(`/api/cuentas/${numeroCuenta}/movimientos?${params}`);
  },
  deposito: (numeroCuenta: string, monto: number) =>
    apiClient.post(`/api/cuentas/${numeroCuenta}/depositos`, {
      monto,
      canalOrigen: 'WEB',
      descripcion: 'Depósito en línea'
    }),
  retiro: (numeroCuenta: string, monto: number) =>
    apiClient.post(`/api/cuentas/${numeroCuenta}/retiros`, {
      monto,
      canalOrigen: 'WEB'
    }),
};