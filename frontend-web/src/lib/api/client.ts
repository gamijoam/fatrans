import axios from 'axios';
import Cookies from 'js-cookie';

export const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  timeout: 10_000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  if (['post', 'put', 'delete', 'patch'].includes(config.method?.toLowerCase() || '')) {
    const csrfToken = Cookies.get('csrf_token');
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
        const newToken = await refreshAccessToken();
        if (newToken) {
          originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
          return apiClient(originalRequest);
        }
      } catch {
        await logout();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = Cookies.get('refresh_token');
  if (!refreshToken) return null;
  try {
    const response = await apiClient.post('/v1/auth/refresh', { refreshToken });
    return response.data.accessToken;
  } catch {
    return null;
  }
}

export async function logout(): Promise<void> {
  try {
    await apiClient.post('/v1/auth/logout');
  } finally {
    Cookies.remove('access_token');
    Cookies.remove('refresh_token');
    Cookies.remove('csrf_token');
    Cookies.remove('usuario');
  }
}
