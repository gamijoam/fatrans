/**
 * Tipos generados desde OpenAPI del backend
 *
 * NOTA: Estos tipos fueron creados manualmente basado en la documentación
 * en docs/base-proyecto/API_CONTRACTS.md
 *
 * Para regenerar tipos automáticamente:
 * 1. Asegurarse que el backend esté corriendo en localhost:18080
 * 2. Ejecutar: npm run generate:types
 *
 * @generated
 */

export type Rol = 'SOCIO' | 'ADMIN' | 'SUPER_ADMIN' | 'CAJERO' | 'ANALISTA_KYC';

// ============ AUTH ============

export interface LoginRequest {
  identificador: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: 900;
  usuario: UsuarioResponse;
}

export interface UsuarioResponse {
  id: string;
  nombreUsuario: string;
  correoElectronico: string;
  nombreCompleto: string;
  rol: Rol;
}

export interface LogoutResponse {
  mensaje: string;
}

export interface MeResponse extends UsuarioResponse {}

export interface RecuperarPasswordRequest {
  correoElectronico: string;
}

export interface RecuperarPasswordResponse {
  mensaje: string;
}

export interface ResetPasswordRequest {
  token: string;
  nuevaPassword: string;
  confirmarPassword: string;
}

export interface ResetPasswordResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: 900;
  mensaje: string;
}

// ============ SOCIOS ============

export interface Direccion {
  calle: string;
  ciudad: string;
  estado: string;
  codigoPostal: string;
  pais: string;
}

export interface ContactoEmergencia {
  nombreCompleto: string;
  telefono: string;
  parentesco: string;
}

export interface SolicitudRegistroRequest {
  primerNombre: string;
  segundoNombre?: string;
  primerApellido: string;
  segundoApellido?: string;
  tipoDocumento: 'CEDULA_IDENTIDAD' | 'PASAPORTE' | 'CEDULA_EXTRANJERA';
  numeroDocumento: string;
  fechaNacimiento: string;
  genero: 'MASCULINO' | 'FEMENINO' | 'OTRO';
  estadoCivil: 'SOLTERO' | 'CASADO' | 'UNION_LIBRE' | 'DIVORCIADO' | 'VIUDO';
  nacionalidad: string;
  direccion: Direccion;
  correoElectronico: string;
  telefonoPrincipal: string;
  telefonoSecundario?: string;
  contactoEmergencia: ContactoEmergencia;
  empresa: string;
  departamento?: string;
  cargo?: string;
  tipoContrato: 'PERMANENTE' | 'TEMPORAL' | 'PRESTACION_SERVICIOS' | 'PASANTE';
  salario: number;
  banco: string;
  numeroCuenta: string;
}

export interface SolicitudRegistroResponse {
  id: string;
  estado: 'PENDIENTE';
  mensaje: string;
  fechaSolicitud: string;
}

export type EstadoSolicitud = 'PENDIENTE' | 'APROBADA' | 'RECHAZADA';

export interface SolicitudListItem {
  id: string;
  primerNombre: string;
  primerApellido: string;
  numeroDocumento: string;
  correoElectronico: string;
  empresa: string;
  estado: EstadoSolicitud;
  fechaSolicitud: string;
}

export interface SolicitudesListResponse {
  content: SolicitudListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface RechazarSolicitudRequest {
  motivo: string;
}

export interface RechazarSolicitudResponse {
  id: string;
  estado: 'RECHAZADA';
  motivo: string;
}

export interface AprobarSolicitudResponse {
  id: string;
  numeroSocio: string;
  primerNombre: string;
  primerApellido: string;
  correoElectronico: string;
  empresa: string;
  estado: 'ACTIVO';
  mensaje: string;
}

// ============ CUENTAS ============

export type TipoCuenta = 'AHORRO' | 'CORRIENTE';

export type EstadoCuenta = 'ACTIVA' | 'BLOQUEADA' | 'CERRADA';

export interface CuentaAhorroResponse {
  numeroCuenta: string;
  tipo: TipoCuenta;
  saldo: number;
  estado: EstadoCuenta;
  fechaCreacion: string;
  titular: {
    id: string;
    nombreCompleto: string;
  };
}

export interface SaldoResponse {
  numeroCuenta: string;
  saldo: number;
  disponible: number;
  bloqueado: number;
}

export interface DepositoRequest {
  monto: number;
}

export interface DepositoResponse {
  numeroCuenta: string;
  monto: number;
  nuevoSaldo: number;
  referencia: string;
  fecha: string;
}

export interface RetiroRequest {
  monto: number;
}

export interface RetiroResponse {
  numeroCuenta: string;
  monto: number;
  nuevoSaldo: number;
  referencia: string;
  fecha: string;
}

export type TipoMovimiento = 'DEPOSITO' | 'RETIRO' | 'TRANSFERENCIA' | 'PAGO_CREDITO';

export interface MovimientoResponse {
  id: string;
  tipo: TipoMovimiento;
  monto: number;
  saldoAnterior: number;
  saldoNuevo: number;
  referencia: string;
  descripcion?: string;
  fecha: string;
}

export interface MovimientosResponse {
  content: MovimientoResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// ============ CRÉDITOS ============

export type TipoAmortizacion = 'FRANCES' | 'AMERICANO' | 'LINEA_RECTA';

export interface TipoCreditoResponse {
  id: string;
  nombre: string;
  descripcion: string;
  tasaInteres: number;
  plazoMinimoMeses: number;
  plazoMaximoMeses: number;
  montoMinimo: number;
  montoMaximo: number;
  tipoAmortizacion: TipoAmortizacion;
  requiereGarante: boolean;
  activo: boolean;
}

export type EstadoSolicitudCredito = 'PENDIENTE' | 'EN_EVALUACION' | 'APROBADA' | 'RECHAZADA' | 'DESEMBOLSADA' | 'CANCELADA';

export interface SolicitudCreditoRequest {
  tipoCreditoId: string;
  montoSolicitado: number;
  plazoMeses: number;
  proposito: string;
  garanteId?: string;
}

export interface SolicitudCreditoResponse {
  id: string;
  numeroSolicitud: string;
  tipoCredito: string;
  montoSolicitado: number;
  plazoMeses: number;
  estado: EstadoSolicitudCredito;
  fechaSolicitud: string;
}

export interface SolicitudCreditoListResponse {
  content: SolicitudCreditoResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CuotaResponse {
  numeroCuota: number;
  fechaVencimiento: string;
  capital: number;
  interes: number;
  monto: number;
  saldoCapital: number;
  estado: 'PENDIENTE' | 'PAGADA' | 'VENCIDA' | 'MORA';
}

export interface CuotasResponse {
  solicitudId: string;
  cuotas: CuotaResponse[];
  totalPendiente: number;
}

export interface PagoCuotaRequest {
  solicitudId: string;
  numeroCuota: number;
  monto: number;
}

export interface PagoCuotaResponse {
  numeroCuota: number;
  montoPagado: number;
  nuevoSaldo: number;
  referencia: string;
  fecha: string;
}

// ============ KYC ============

export type TipoDocumentoKYC = 'CEDULA_ANVERSO' | 'CEDULA_REVERSO' | 'SELFIE_CEDULA' | 'COMPROBANTE_DOMICILIO';

export type EstadoVerificacion = 'NO_INICIADO' | 'EN_PROCESO' | 'VERIFICADO' | 'RECHAZADO';

export interface VerificacionKYCResponse {
  estado: EstadoVerificacion;
  porcentajeCompletado: number;
  documentosRecibidos: number;
  documentosRequeridos: number;
  fechaVerificacion?: string;
}

export interface DocumentoKYCResponse {
  id: string;
  tipo: TipoDocumentoKYC;
  nombreArchivo: string;
  url: string;
  fechaSubida: string;
  estado: 'RECIBIDO' | 'VERIFICADO' | 'RECHAZADO';
}

export interface SubirDocumentoRequest {
  tipo: TipoDocumentoKYC;
  archivo: File;
}

// ============ BENEFICIARIOS ============

export type Parentesco = 'CONYUGE' | 'HIJO' | 'PADRE' | 'MADRE' | 'HERMANO' | 'SOBRINO' | 'ABUELO' | 'OTRO';

export interface BeneficiarioRequest {
  nombreCompleto: string;
  numeroDocumento: string;
  parentesco: Parentesco;
  porcentaje: number;
  telefono?: string;
  correoElectronico?: string;
}

export interface BeneficiarioResponse {
  id: string;
  nombreCompleto: string;
  numeroDocumento: string;
  parentesco: Parentesco;
  porcentaje: number;
  telefono?: string;
  correoElectronico?: string;
  fechaCreacion: string;
}

export interface BeneficiariosListResponse {
  content: BeneficiarioResponse[];
  totalElements: number;
  totalPages: number;
}

// ============ DOCUMENTOS ============

export type TipoDocumento = 'ESTADO_CUENTA' | 'CONSTANCIA_AFILIACION' | 'CARTA_BENEFICIARIOS' | 'CONTRATO_ADHESION' | 'PAGARE';

export interface GenerarDocumentoRequest {
  tipo: TipoDocumento;
  parametros?: Record<string, string>;
}

export interface DocumentoResponse {
  id: string;
  tipo: TipoDocumento;
  nombre: string;
  fechaGeneracion: string;
  url?: string;
}

export interface DocumentosListResponse {
  content: DocumentoResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// ============ PAGINATION ============

export interface PageRequest {
  page?: number;
  size?: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// ============ ERROR ============

export interface ApiError {
  codigo: string;
  mensaje: string;
  timestamp: string;
  path?: string;
}
