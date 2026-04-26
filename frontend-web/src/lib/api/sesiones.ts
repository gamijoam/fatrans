import { apiClient } from './client';

export interface SesionInfo {
  id: string;
  ipAddress: string | null;
  userAgent: string | null;
  ultimoAcceso: string;
  fechaCreacion: string;
  expiraAt: string;
  activa: boolean;
}

export interface SesionInvalidationResponse {
  usuarioId: string;
  sesionesInvalidadas: number;
  mensaje: string;
}

export const sesionesApi = {
  listarPorUsuario: (usuarioId: string) =>
    apiClient.get<SesionInfo[]>(`/api/v1/admin/sesiones/usuario/${usuarioId}`),

  invalidarTodas: (usuarioId: string) =>
    apiClient.post<SesionInvalidationResponse>(`/api/v1/admin/sesiones/usuario/${usuarioId}/invalidar-todas`),

  invalidarSesion: (sesionId: string) =>
    apiClient.post(`/api/v1/admin/sesiones/${sesionId}/invalidar`),
};