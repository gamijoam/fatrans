'use client';

import { useEffect, useState, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Loader2, ArrowLeft, User, Mail, Phone, MapPin, Building,
  Calendar, CreditCard, Users, Shield, AlertCircle, CheckCircle, XCircle,
  FileCheck, Clock, AlertTriangle, Check
} from 'lucide-react';
import { toast } from 'sonner';

interface Socio {
  id: string;
  numeroSocio: string;
  tipoDocumento: string;
  numeroDocumento: string;
  primerNombre: string;
  segundoNombre: string;
  primerApellido: string;
  segundoApellido: string;
  correoElectronico: string;
  telefonoPrincipal: string;
  telefonoSecundario: string;
  empresa: string;
  departamento: string;
  cargo: string;
  tipoContrato: string;
  salario: number;
  montoAhorro: number;
  estado: string;
  fechaIngreso: string;
  fechaRegistro: string;
  fechaActivacion: string | null;
  fechaDesactivacion: string | null;
  motivoDesactivacion: string | null;
}

interface Cuenta {
  id: string;
  numeroCuenta: string;
  tipoCuenta: string;
  saldo: number;
  estado: string;
}

interface Beneficiario {
  id: string;
  nombreCompleto: string;
  tipoDocumento: string;
  numeroDocumento: string;
  parentesco: string;
  porcentaje: number;
  telefono: string;
  activo: boolean;
}

interface Credito {
  id: string;
  numeroSolicitud: string;
  tipoCreditoNombre: string;
  montoSolicitado: number;
  plazoMeses: number;
  estado: string;
  createdAt: string;
}

interface KycData {
  verificacionId?: string;
  socioId: string;
  nivel?: string;
  estado: string;
  fechaInicio?: string;
  fechaCompletado?: string;
  fechaExpiracion?: string;
  revisadoPor?: string;
  motivoRechazo?: string;
  mensaje?: string;
}

const ESTADO_COLORS: Record<string, string> = {
  ACTIVO: 'bg-green-100 text-green-800 border-green-300',
  INACTIVO: 'bg-red-100 text-red-800 border-red-300',
  PENDIENTE: 'bg-yellow-100 text-yellow-800 border-yellow-300',
  ELIMINADO: 'bg-gray-100 text-gray-800 border-gray-300',
};

const ESTADO_CREDITO_COLORS: Record<string, string> = {
  PENDIENTE: 'bg-yellow-100 text-yellow-800',
  EN_EVALUACION: 'bg-blue-100 text-blue-800',
  APROBADA: 'bg-green-100 text-green-800',
  RECHAZADA: 'bg-red-100 text-red-800',
  DESEMBOLSADO: 'bg-purple-100 text-purple-800',
};

function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '-';
  try {
    return new Date(dateStr).toLocaleDateString('es-VE');
  } catch {
    return dateStr;
  }
}

function formatCurrency(value: number): string {
  return new Intl.NumberFormat('es-VE', {
    style: 'currency',
    currency: 'VES',
    minimumFractionDigits: 2,
  }).format(value);
}

export default function SocioDetallePage() {
  const params = useParams();
  const router = useRouter();
  const socioId = params.id as string;

  const [socio, setSocio] = useState<Socio | null>(null);
  const [cuentas, setCuentas] = useState<Cuenta[]>([]);
  const [beneficiarios, setBeneficiarios] = useState<Beneficiario[]>([]);
  const [creditos, setCreditos] = useState<Credito[]>([]);
  const [kyc, setKyc] = useState<KycData | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [showDesactivarModal, setShowDesactivarModal] = useState(false);
  const [motivoDesactivar, setMotivoDesactivar] = useState('');

  const cargarDatos = useCallback(async () => {
    setLoading(true);
    try {
      const [socioRes, cuentasRes, beneficiariosRes, creditosRes, kycRes] = await Promise.all([
        fetch(`/api/admin/socios/${socioId}`, { credentials: 'include' }),
        fetch(`/api/admin/socios/${socioId}/cuentas`, { credentials: 'include' }),
        fetch(`/api/admin/socios/${socioId}/beneficiarios`, { credentials: 'include' }),
        fetch(`/api/admin/socios/${socioId}/creditos`, { credentials: 'include' }),
        fetch(`/api/admin/socios/${socioId}/kyc`, { credentials: 'include' }),
      ]);

      if (!socioRes.ok) throw new Error('Error al cargar socio');

      setSocio(await socioRes.json());
      if (cuentasRes.ok) setCuentas(await cuentasRes.json());
      if (beneficiariosRes.ok) {
        const benData = await beneficiariosRes.json();
        setBeneficiarios(benData.beneficiarios || []);
      }
      if (creditosRes.ok) setCreditos(await creditosRes.json());
      if (kycRes.ok) {
        const kycData = await kycRes.json();
        setKyc(kycData);
      }

    } catch (err) {
      console.error('Error cargando datos:', err);
      toast.error('Error al cargar datos del socio');
    } finally {
      setLoading(false);
    }
  }, [socioId]);

  useEffect(() => {
    cargarDatos();
  }, [cargarDatos]);

  const handleActivar = async () => {
    setUpdating(true);
    try {
      const res = await fetch(`/api/admin/socios/${socioId}/action`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ action: 'activar' }),
      });
      if (!res.ok) throw new Error();
      toast.success('Socio activado correctamente');
      cargarDatos();
    } catch {
      toast.error('Error al activar socio');
    } finally {
      setUpdating(false);
    }
  };

  const handleDesactivar = async () => {
    if (!motivoDesactivar.trim()) {
      toast.error('Ingrese un motivo para la desactivación');
      return;
    }
    setUpdating(true);
    try {
      const res = await fetch(`/api/admin/socios/${socioId}/action`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ action: 'desactivar', motivo: motivoDesactivar }),
      });
      if (!res.ok) throw new Error();
      toast.success('Socio desactivado correctamente');
      setShowDesactivarModal(false);
      cargarDatos();
    } catch {
      toast.error('Error al desactivar socio');
    } finally {
      setUpdating(false);
    }
  };

  const getNombreCompleto = () => {
    if (!socio) return '';
    const parts = [socio.primerNombre, socio.segundoNombre, socio.primerApellido, socio.segundoApellido].filter(Boolean);
    return parts.join(' ');
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  if (!socio) {
    return (
      <div className="p-6 text-center">
        <AlertCircle className="h-12 w-12 mx-auto text-red-500 mb-4" />
        <h2 className="text-xl font-bold text-gray-900">Socio no encontrado</h2>
        <Link href="/admin/socios" className="text-green-600 hover:underline mt-4 inline-block">
          Volver a la lista
        </Link>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => router.push('/admin/socios')}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Volver
          </Button>
          <h1 className="text-2xl font-bold text-gray-900">Detalle de Socio</h1>
        </div>
        <div className="flex gap-2">
          {socio.estado === 'ACTIVO' ? (
            <Button variant="destructive" onClick={() => setShowDesactivarModal(true)} disabled={updating}>
              <XCircle className="h-4 w-4 mr-2" />
              Desactivar
            </Button>
          ) : (
            <Button onClick={handleActivar} disabled={updating}>
              <CheckCircle className="h-4 w-4 mr-2" />
              Activar
            </Button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-1">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User className="h-5 w-5" />
              Información Personal
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-3">
              <Badge className={ESTADO_COLORS[socio.estado] || 'bg-gray-100'}>
                {socio.estado}
              </Badge>
              <span className="text-sm text-gray-500">N° {socio.numeroSocio || '-'}</span>
            </div>

            <div>
              <p className="text-lg font-semibold">{getNombreCompleto()}</p>
              <p className="text-sm text-gray-500">{socio.tipoDocumento} {socio.numeroDocumento}</p>
            </div>

            <div className="space-y-2">
              <div className="flex items-center gap-2 text-sm">
                <Mail className="h-4 w-4 text-gray-400" />
                <span>{socio.correoElectronico || '-'}</span>
              </div>
              <div className="flex items-center gap-2 text-sm">
                <Phone className="h-4 w-4 text-gray-400" />
                <span>{socio.telefonoPrincipal} {socio.telefonoSecundario ? `/ ${socio.telefonoSecundario}` : ''}</span>
              </div>
              <div className="flex items-center gap-2 text-sm">
                <Building className="h-4 w-4 text-gray-400" />
                <span>{socio.empresa || '-'}</span>
              </div>
              <div className="flex items-center gap-2 text-sm">
                <Calendar className="h-4 w-4 text-gray-400" />
                <span>Ingreso: {formatDate(socio.fechaIngreso)}</span>
              </div>
            </div>

            {socio.estado === 'INACTIVO' && socio.motivoDesactivacion && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-sm text-red-700">
                  <strong>Motivo:</strong> {socio.motivoDesactivacion}
                </p>
                <p className="text-xs text-red-500 mt-1">
                  Fecha: {formatDate(socio.fechaDesactivacion)}
                </p>
              </div>
            )}

            <div className="pt-4 border-t">
              <p className="text-sm font-medium text-gray-700 mb-2">Datos Laborales</p>
              <div className="space-y-1 text-sm text-gray-600">
                <p><strong>Cargo:</strong> {socio.cargo || '-'}</p>
                <p><strong>Departamento:</strong> {socio.departamento || '-'}</p>
                <p><strong>Tipo Contrato:</strong> {socio.tipoContrato || '-'}</p>
                <p><strong>Salario:</strong> {formatCurrency(socio.salario)}</p>
              </div>
            </div>

            <div className="pt-4 border-t">
              <p className="text-sm font-medium text-gray-700 mb-2">Ahorros</p>
              <p className="text-2xl font-bold text-green-600">{formatCurrency(socio.montoAhorro)}</p>
            </div>
          </CardContent>
        </Card>

        <div className="lg:col-span-2">
          <Tabs defaultValue="cuentas" className="w-full">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="cuentas">Cuentas</TabsTrigger>
              <TabsTrigger value="beneficiarios">Beneficiarios</TabsTrigger>
              <TabsTrigger value="creditos">Créditos</TabsTrigger>
              <TabsTrigger value="kyc">KYC</TabsTrigger>
            </TabsList>

            <TabsContent value="cuentas" className="mt-4">
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <CreditCard className="h-5 w-5" />
                    Cuentas de Ahorro
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {cuentas.length === 0 ? (
                    <p className="text-center text-gray-500 py-8">No tiene cuentas asociadas</p>
                  ) : (
                    <div className="space-y-3">
                      {cuentas.map((cuenta) => (
                        <div key={cuenta.id} className="flex items-center justify-between p-4 border rounded-lg">
                          <div>
                            <p className="font-mono font-medium">{cuenta.numeroCuenta}</p>
                            <p className="text-sm text-gray-500">{cuenta.tipoCuenta}</p>
                          </div>
                          <div className="text-right">
                            <p className="text-xl font-bold text-green-600">{formatCurrency(cuenta.saldo)}</p>
                            <Badge className={ESTADO_COLORS[cuenta.estado] || 'bg-gray-100'}>
                              {cuenta.estado}
                            </Badge>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="beneficiarios" className="mt-4">
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Users className="h-5 w-5" />
                    Beneficiarios
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {beneficiarios.length === 0 ? (
                    <p className="text-center text-gray-500 py-8">No tiene beneficiarios registrados</p>
                  ) : (
                    <div className="overflow-x-auto">
                      <table className="w-full text-sm">
                        <thead>
                          <tr className="border-b bg-gray-50">
                            <th className="text-left p-3">Nombre</th>
                            <th className="text-left p-3">Documento</th>
                            <th className="text-left p-3">Parentesco</th>
                            <th className="text-right p-3">%</th>
                            <th className="text-center p-3">Estado</th>
                          </tr>
                        </thead>
                        <tbody>
                          {beneficiarios.map((b) => (
                            <tr key={b.id} className="border-b">
                              <td className="p-3">{b.nombreCompleto}</td>
                              <td className="p-3">{b.tipoDocumento} {b.numeroDocumento}</td>
                              <td className="p-3">{b.parentesco}</td>
                              <td className="p-3 text-right font-medium">{b.porcentaje}%</td>
                              <td className="p-3 text-center">
                                <Badge className={b.activo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}>
                                  {b.activo ? 'Activo' : 'Inactivo'}
                                </Badge>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="creditos" className="mt-4">
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <CreditCard className="h-5 w-5" />
                    Solicitudes de Crédito
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {creditos.length === 0 ? (
                    <p className="text-center text-gray-500 py-8">No tiene solicitudes de crédito</p>
                  ) : (
                    <div className="overflow-x-auto">
                      <table className="w-full text-sm">
                        <thead>
                          <tr className="border-b bg-gray-50">
                            <th className="text-left p-3">N° Solicitud</th>
                            <th className="text-left p-3">Tipo</th>
                            <th className="text-right p-3">Monto</th>
                            <th className="text-center p-3">Plazo</th>
                            <th className="text-center p-3">Estado</th>
                            <th className="text-center p-3">Fecha</th>
                          </tr>
                        </thead>
                        <tbody>
                          {creditos.map((c) => (
                            <tr key={c.id} className="border-b hover:bg-gray-50">
                              <td className="p-3 font-mono text-xs">{c.numeroSolicitud}</td>
                              <td className="p-3">{c.tipoCreditoNombre}</td>
                              <td className="p-3 text-right font-medium">{formatCurrency(c.montoSolicitado)}</td>
                              <td className="p-3 text-center">{c.plazoMeses} meses</td>
                              <td className="p-3 text-center">
                                <Badge className={ESTADO_CREDITO_COLORS[c.estado] || 'bg-gray-100'}>
                                  {c.estado}
                                </Badge>
                              </td>
                              <td className="p-3 text-center text-xs text-gray-500">
                                {formatDate(c.createdAt)}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="kyc" className="mt-4">
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Shield className="h-5 w-5" />
                    Verificación KYC
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {!kyc ? (
                    <div className="flex justify-center py-8">
                      <Loader2 className="h-6 w-6 animate-spin text-green-600" />
                    </div>
                  ) : kyc.estado === 'SIN_KYC' ? (
                    <div className="text-center py-8">
                      <AlertCircle className="h-12 w-12 mx-auto text-yellow-400 mb-4" />
                      <p className="text-gray-500">El socio no tiene verificaciones KYC</p>
                      <p className="text-sm text-gray-400 mt-2">
                        El socio aún no ha iniciado el proceso de verificación de identidad
                      </p>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <div className="bg-gray-50 rounded-lg p-4">
                          <p className="text-xs text-gray-500 mb-1">Nivel</p>
                          <p className="font-semibold text-sm">{kyc.nivel || '-'}</p>
                        </div>
                        <div className="bg-gray-50 rounded-lg p-4">
                          <p className="text-xs text-gray-500 mb-1">Estado</p>
                          <Badge className={kyc.estado === 'APROBADO' ? 'bg-green-100 text-green-800' : kyc.estado === 'RECHAZADO' ? 'bg-red-100 text-red-800' : 'bg-yellow-100 text-yellow-800'}>
                            {kyc.estado}
                          </Badge>
                        </div>
                        <div className="bg-gray-50 rounded-lg p-4">
                          <p className="text-xs text-gray-500 mb-1">Fecha Inicio</p>
                          <p className="font-semibold text-sm">{formatDate(kyc.fechaInicio)}</p>
                        </div>
                        <div className="bg-gray-50 rounded-lg p-4">
                          <p className="text-xs text-gray-500 mb-1">Expira</p>
                          <p className="font-semibold text-sm">{formatDate(kyc.fechaExpiracion)}</p>
                        </div>
                      </div>

                      {kyc.estado === 'APROBADO' && (
                        <div className="flex items-center gap-2 p-3 bg-green-50 border border-green-200 rounded-lg">
                          <CheckCircle className="h-5 w-5 text-green-600" />
                          <div>
                            <p className="text-sm font-medium text-green-800">Verificación Aprobada</p>
                            <p className="text-xs text-green-600">Revisado por: {kyc.revisadoPor || 'Sistema'}</p>
                          </div>
                        </div>
                      )}

                      {kyc.estado === 'RECHAZADO' && (
                        <div className="flex items-center gap-2 p-3 bg-red-50 border border-red-200 rounded-lg">
                          <XCircle className="h-5 w-5 text-red-600" />
                          <div>
                            <p className="text-sm font-medium text-red-800">Verificación Rechazada</p>
                            <p className="text-xs text-red-600">Motivo: {kyc.motivoRechazo || 'No especificado'}</p>
                          </div>
                        </div>
                      )}

                      {(kyc.estado === 'PENDIENTE' || kyc.estado === 'EN_REVISION') && (
                        <div className="flex items-center gap-2 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
                          <Clock className="h-5 w-5 text-yellow-600" />
                          <div>
                            <p className="text-sm font-medium text-yellow-800">En Proceso</p>
                            <p className="text-xs text-yellow-600">La verificación está siendo revisada</p>
                          </div>
                        </div>
                      )}

                      {kyc.verificacionId && (
                        <div className="pt-4 border-t">
                          <Link href={`/admin/kyc/${kyc.verificacionId}`}>
                            <Button variant="outline" size="sm">
                              <FileCheck className="h-4 w-4 mr-2" />
                              Ver Detalle Completo KYC
                            </Button>
                          </Link>
                        </div>
                      )}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </div>

      {showDesactivarModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-md mx-4 p-6">
            <h3 className="text-lg font-semibold mb-4">Desactivar Socio</h3>
            <p className="text-sm text-gray-500 mb-4">
              Esta acción desactivará al socio. Ingrese el motivo:
            </p>
            <textarea
              className="w-full p-3 border rounded-lg text-sm"
              rows={3}
              value={motivoDesactivar}
              onChange={(e) => setMotivoDesactivar(e.target.value)}
              placeholder="Motivo de la desactivación..."
            />
            <div className="flex gap-3 mt-4">
              <Button variant="outline" onClick={() => setShowDesactivarModal(false)} className="flex-1">
                Cancelar
              </Button>
              <Button variant="destructive" onClick={handleDesactivar} disabled={updating} className="flex-1">
                {updating ? <Loader2 className="h-4 w-4 mr-2 animate-spin" /> : null}
                Desactivar
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}