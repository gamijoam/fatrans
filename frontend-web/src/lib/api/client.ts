import axios from "axios";
import { toast } from "sonner";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:18080/api";

export function resolveApiAssetUrl(url?: string | null) {
  if (!url) return "";
  if (/^https?:\/\//i.test(url)) return url;
  if (url.startsWith("/api/")) {
    try {
      return `${new URL(API_URL).origin}${url}`;
    } catch {
      return url;
    }
  }
  if (url.startsWith("/v1/")) {
    return `${API_URL}${url}`;
  }
  return url;
}

export const apiClient = axios.create({
  baseURL: API_URL,
  timeout: 10_000,
  withCredentials: true,
});

apiClient.interceptors.request.use((config) => {
  const method = config.method?.toLowerCase();
  if (["post", "put", "delete", "patch"].includes(method || "")) {
    const csrfToken =
      typeof document !== "undefined"
        ? document.cookie
            .split("; ")
            .find((row) => row.startsWith("csrf_token="))
            ?.split("=")[1]
        : null;

    if (csrfToken) {
      config.headers["X-CSRF-Token"] = csrfToken;
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
        await apiClient.post("/v1/auth/refresh-web");
        return apiClient(originalRequest);
      } catch {
        toast.error("Sesión expirada. Por favor inicie sesión nuevamente.");
        if (typeof window !== "undefined") {
          window.location.href = "/login";
        }
      }
    }

    const message =
      error.response?.data?.message || error.message || "Error desconocido";
    // No mostrar toast para errores 404 de recursos no encontrados en dashboard (silencioso)
    if (error.response?.status !== 404) {
      toast.error(message);
    }

    return Promise.reject(error);
  },
);

export const authApi = {
  login: (identificador: string, password: string) =>
    apiClient.post("/v1/auth/login-web", { identificador, password }),
  logout: () => apiClient.post("/v1/auth/logout-web"),
  refresh: () => apiClient.post("/v1/auth/refresh-web"),
  me: () => apiClient.get("/v1/auth/me"),
  changePassword: (passwordActual: string, nuevoPassword: string) =>
    apiClient.post("/v1/auth/cambiar-password", {
      passwordActual,
      nuevoPassword,
    }),
};

export const sociosApi = {
  getAll: () => apiClient.get("/v1/socios"),
  getById: (id: string) => apiClient.get(`/v1/socios/${id}`),
  updateProfile: (id: string, data: unknown) =>
    apiClient.put(`/v1/socios/${id}/perfil`, data),
};

export const cuentasApi = {
  getCuentas: (socioId: string) =>
    apiClient.get(`/v1/cuentas/socio/${socioId}`),
  getCuenta: (numeroCuenta: string) =>
    apiClient.get(`/v1/cuentas/${numeroCuenta}`),
  getSaldo: (numeroCuenta: string) =>
    apiClient.get(`/v1/cuentas/${numeroCuenta}/saldo`),
  getMovimientos: (
    numeroCuenta: string,
    page = 0,
    size = 10,
    fechaInicio?: string,
    fechaFin?: string,
    tipoFiltro?: string,
  ) => {
    let url = `/v1/cuentas/${numeroCuenta}/movimientos?page=${page}&size=${size}`;
    if (fechaInicio) url += `&fechaInicio=${fechaInicio}`;
    if (fechaFin) url += `&fechaFin=${fechaFin}`;
    if (tipoFiltro) url += `&tipo=${tipoFiltro}`;
    return apiClient.get(url);
  },
  // Issue #218 PR-C — `extras` permite enviar la declaración LOCDOFT
  // cuando el backend responde 422 LOCDOFT_CONSENT_REQUIRED y el usuario
  // confirma en el modal. Para operaciones normales, no se pasa.
  deposito: (
    numeroCuenta: string,
    monto: number,
    extras?: { confirmaOrigenLicito?: boolean; origenFondos?: string },
  ) =>
    apiClient.post(`/v1/cuentas/${numeroCuenta}/depositos`, {
      monto,
      canalOrigen: "WEB",
      descripcion: "Depósito en línea",
      ...(extras?.confirmaOrigenLicito
        ? {
            confirmaOrigenLicito: true,
            origenFondos: extras.origenFondos || null,
          }
        : {}),
    }),
  retiro: (
    numeroCuenta: string,
    monto: number,
    extras?: { confirmaOrigenLicito?: boolean; origenFondos?: string },
  ) =>
    apiClient.post(`/v1/cuentas/${numeroCuenta}/retiros`, {
      monto,
      canalOrigen: "WEB",
      descripcion: "Retiro en línea",
      ...(extras?.confirmaOrigenLicito
        ? {
            confirmaOrigenLicito: true,
            origenFondos: extras.origenFondos || null,
          }
        : {}),
    }),
};

export const creditosApi = {
  getTiposCredito: () => apiClient.get("/v1/creditos/tipos-credito"),
  getSolicitudesAdmin: (params?: Record<string, string>) => {
    const query = params ? new URLSearchParams(params).toString() : "";
    return apiClient.get(
      `/v1/admin/creditos/solicitudes${query ? `?${query}` : ""}`,
    );
  },
  getSolicitudesPorSocio: (socioId: string) =>
    apiClient.get(`/v1/creditos/solicitudes/socio/${socioId}`),
  getSolicitud: (numero: string) =>
    apiClient.get(`/v1/creditos/solicitudes/${numero}`),
  crearSolicitud: (data: unknown) =>
    apiClient.post("/v1/creditos/solicitudes", data),
  simular: (data: unknown) => apiClient.post("/v1/simulador", data),
};

export const productosApi = {
  getPublicados: () => apiClient.get("/v1/productos"),
  getProducto: (slug: string) => apiClient.get(`/v1/productos/${slug}`),
  precalificar: (id: number) =>
    apiClient.post(`/v1/productos/${id}/precalificar`),
  solicitarFinanciamiento: (id: number) =>
    apiClient.post(`/v1/productos/${id}/solicitar-financiamiento`),
  getAdmin: () => apiClient.get("/v1/admin/productos"),
  crear: (data: unknown) => apiClient.post("/v1/admin/productos", data),
  actualizar: (id: number, data: unknown) =>
    apiClient.put(`/v1/admin/productos/${id}`, data),
  subirImagen: (id: number, file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return apiClient.post(`/v1/admin/productos/${id}/imagen`, formData, {
      timeout: 30_000,
    });
  },
  agregarImagen: (id: number, file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return apiClient.post(`/v1/admin/productos/${id}/imagenes`, formData, {
      timeout: 30_000,
    });
  },
  marcarImagenPrincipal: (id: number, imagenId: number) =>
    apiClient.post(`/v1/admin/productos/${id}/imagenes/${imagenId}/principal`),
  eliminarImagen: (id: number, imagenId: number) =>
    apiClient.delete(`/v1/admin/productos/${id}/imagenes/${imagenId}`),
  publicar: (id: number) =>
    apiClient.post(`/v1/admin/productos/${id}/publicar`),
  pausar: (id: number) => apiClient.post(`/v1/admin/productos/${id}/pausar`),
  archivar: (id: number) =>
    apiClient.post(`/v1/admin/productos/${id}/archivar`),
};

export const adminApi = {
  getStats: () => apiClient.get("/v1/admin/dashboard/estadisticas"),
  getActividad: (limit: number = 15) =>
    apiClient.get(`/v1/admin/actividad?limit=${limit}`),
  getSolicitudes: (params?: Record<string, string>) => {
    const query = params ? new URLSearchParams(params).toString() : "";
    return apiClient.get(
      `/v1/admin/creditos/solicitudes${query ? `?${query}` : ""}`,
    );
  },
};
