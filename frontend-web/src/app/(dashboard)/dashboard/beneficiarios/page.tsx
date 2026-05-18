'use client';

import { useEffect, useState, useCallback } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useAuthStore } from '@/stores/auth-store';
import {
  Users, Plus, Edit2, Trash2, Loader2, AlertTriangle,
  Check, User, Phone, Percent, AlertCircle
} from 'lucide-react';
import { toast } from 'sonner';

interface Beneficiario {
  id: string;
  socioId: string;
  nombreCompleto: string;
  numeroDocumento: string;
  tipoDocumento: 'CEDULA_IDENTIDAD' | 'RIF' | 'PASAPORTE' | 'CEDULA_EXTRANJERO';
  parentesco: 'CONYUGE' | 'HIJO' | 'PADRE' | 'MADRE' | 'HERMANO' | 'ABUELO' | 'NIETO' | 'SOBRINO' | 'TIO' | 'OTRO';
  porcentaje: number;
  telefono?: string;
  activo: boolean;
  fechaRegistro: string;
  fechaActualizacion: string;
}

interface BeneficiariosResponse {
  beneficiarios: Beneficiario[];
  total: number;
  sumaPorcentajes: number;
}

const TIPO_DOCUMENTO_LABELS: Record<string, string> = {
  CEDULA_IDENTIDAD: 'Cédula',
  RIF: 'RIF',
  PASAPORTE: 'Pasaporte',
  CEDULA_EXTRANJERO: 'Cédula Extranjero',
};

const PARENTESCO_LABELS: Record<string, string> = {
  CONYUGE: 'Cónyuge',
  HIJO: 'Hijo/a',
  PADRE: 'Padre',
  MADRE: 'Madre',
  HERMANO: 'Hermano/a',
  ABUELO: 'Abuelo/a',
  NIETO: 'Nieto/a',
  SOBRINO: 'Sobrino/a',
  TIO: 'Tío/a',
  OTRO: 'Otro',
};

interface FormData {
  nombreCompleto: string;
  numeroDocumento: string;
  tipoDocumento: 'CEDULA_IDENTIDAD' | 'RIF' | 'PASAPORTE' | 'CEDULA_EXTRANJERO';
  parentesco: 'CONYUGE' | 'HIJO' | 'PADRE' | 'MADRE' | 'HERMANO' | 'ABUELO' | 'NIETO' | 'SOBRINO' | 'TIO' | 'OTRO';
  porcentaje: string;
  telefono: string;
}

const INITIAL_FORM_DATA: FormData = {
  nombreCompleto: '',
  numeroDocumento: '',
  tipoDocumento: 'CEDULA_IDENTIDAD',
  parentesco: 'CONYUGE',
  porcentaje: '',
  telefono: '',
};

export default function DashboardBeneficiariosPagina() {
  const user = useAuthStore((state) => state.user);
  const [beneficiarios, setBeneficiarios] = useState<Beneficiario[]>([]);
  const [loading, setLoading] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [formData, setFormData] = useState<FormData>(INITIAL_FORM_DATA);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState<string | null>(null);

  const socioId = user?.socioId || '';

  const loadBeneficiarios = useCallback(async () => {
    if (!socioId) {
      setLoading(false);
      return;
    }
    try {
      const res = await fetch(`/api/beneficiarios?socioId=${socioId}`, { credentials: 'include' });
      if (res.ok) {
        const data: BeneficiariosResponse = await res.json();
        setBeneficiarios(data.beneficiarios || []);
      } else if (res.status === 404) {
        setBeneficiarios([]);
      } else {
        toast.error('Error al cargar beneficiarios');
      }
    } catch {
      toast.error('Error al cargar beneficiarios');
    } finally {
      setLoading(false);
    }
  }, [socioId]);

  useEffect(() => {
    loadBeneficiarios();
  }, [loadBeneficiarios]);

  const resetForm = () => {
    setFormData(INITIAL_FORM_DATA);
    setEditingId(null);
  };

  const handleOpenModal = (beneficiario?: Beneficiario) => {
    if (beneficiario) {
      setEditingId(beneficiario.id);
      setFormData({
        nombreCompleto: beneficiario.nombreCompleto,
        numeroDocumento: beneficiario.numeroDocumento,
        tipoDocumento: beneficiario.tipoDocumento,
        parentesco: beneficiario.parentesco,
        porcentaje: String(beneficiario.porcentaje),
        telefono: beneficiario.telefono || '',
      });
    } else {
      resetForm();
    }
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    resetForm();
  };

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const calcularSumaPorcentajes = (nuevoPorcentaje?: string, beneficiarioId?: string) => {
    return beneficiarios.reduce((acc, b) => {
      if (beneficiarioId && b.id === beneficiarioId) return acc;
      return acc + b.porcentaje;
    }, 0) + (nuevoPorcentaje ? parseFloat(nuevoPorcentaje) : 0);
  };

  const handleSave = async () => {
    if (!socioId) {
      toast.error('Socio ID no disponible');
      return;
    }

    if (!formData.nombreCompleto.trim()) {
      toast.error('Nombre completo es requerido');
      return;
    }

    if (!formData.numeroDocumento.trim()) {
      toast.error('Número de documento es requerido');
      return;
    }

    const porcentaje = parseFloat(formData.porcentaje);
    if (isNaN(porcentaje) || porcentaje < 0.01 || porcentaje > 100) {
      toast.error('Porcentaje debe estar entre 0.01 y 100');
      return;
    }

    const sumaActual = beneficiarios.reduce((acc, b) => acc + b.porcentaje, 0);
    const sumaProxima = editingId
      ? sumaActual - (beneficiarios.find(b => b.id === editingId)?.porcentaje || 0) + porcentaje
      : sumaActual + porcentaje;

    if (sumaProxima > 100) {
      const maximoDisponible = editingId
        ? 100 - (sumaActual - (beneficiarios.find(b => b.id === editingId)?.porcentaje || 0))
        : 100 - sumaActual;
      toast.error(`La suma de porcentajes no puede exceder 100%. Máximo disponible: ${maximoDisponible.toFixed(2)}%`);
      return;
    }

    if (beneficiarios.length >= 5 && !editingId) {
      toast.error('Máximo 5 beneficiarios activos permitidos');
      return;
    }

    setGuardando(true);
    try {
      const payload = {
        socioId,
        nombreCompleto: formData.nombreCompleto.trim(),
        numeroDocumento: formData.numeroDocumento.trim(),
        tipoDocumento: formData.tipoDocumento,
        parentesco: formData.parentesco,
        porcentaje: formData.porcentaje,
        telefono: formData.telefono.trim() || undefined,
      };

      const url = editingId
        ? `/api/beneficiarios/${editingId}`
        : '/api/beneficiarios';
      const method = editingId ? 'PUT' : 'POST';

      if (editingId) {
        (payload as any).socioId = socioId;
      }

      const res = await fetch(url, {
        method,
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (res.ok) {
        toast.success(editingId ? 'Beneficiario actualizado' : 'Beneficiario creado');
        handleCloseModal();
        loadBeneficiarios();
      } else {
        const error = await res.json();
        toast.error(error.message || 'Error al guardar');
      }
    } catch {
      toast.error('Error al guardar beneficiario');
    } finally {
      setGuardando(false);
    }
  };

  const handleDelete = async (id: string) => {
    setShowDeleteConfirm(null);
    try {
      const res = await fetch(`/api/beneficiarios/${id}?socioId=${socioId}`, {
        method: 'DELETE',
        credentials: 'include',
      });

      if (res.ok) {
        toast.success('Beneficiario eliminado');
        loadBeneficiarios();
      } else {
        const error = await res.json();
        toast.error(error.message || 'Error al eliminar');
      }
    } catch {
      toast.error('Error al eliminar beneficiario');
    }
  };

  const sumaPorcentajes = beneficiarios.reduce((acc, b) => acc + b.porcentaje, 0);
  const porcentajeRestante = 100 - sumaPorcentajes;
  const isPorcentajeValido = Math.abs(sumaPorcentajes - 100) < 0.01;

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <div className="p-2 bg-blue-100 rounded-lg">
            <Users className="h-6 w-6 text-blue-600" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Beneficiarios</h1>
            <p className="text-sm text-gray-500">Gestione sus beneficiarios designados</p>
          </div>
        </div>
        <Button
          onClick={() => handleOpenModal()}
          disabled={beneficiarios.length >= 5}
          className="bg-green-600 hover:bg-green-700"
        >
          <Plus className="h-4 w-4 mr-2" />
          Agregar Beneficiario
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Percent className="h-5 w-5" />
            Resumen de Porcentajes
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-4">
            <div className="flex-1">
              <div className="h-4 bg-gray-200 rounded-full overflow-hidden">
                <div
                  className={`h-full transition-all ${isPorcentajeValido ? 'bg-green-600' : 'bg-yellow-500'}`}
                  style={{ width: `${Math.min(sumaPorcentajes, 100)}%` }}
                />
              </div>
            </div>
            <span className={`font-bold ${isPorcentajeValido ? 'text-green-600' : 'text-yellow-600'}`}>
              {sumaPorcentajes.toFixed(2)}%
            </span>
          </div>
          {!isPorcentajeValido && (
            <div className="mt-3 p-3 bg-yellow-50 border border-yellow-200 rounded-lg flex items-start gap-2">
              <AlertTriangle className="h-5 w-5 text-yellow-600 flex-shrink-0 mt-0.5" />
              <div>
                <p className="text-sm font-medium text-yellow-800">Distribución incompleta</p>
                <p className="text-sm text-yellow-700">
                  Porcentaje restante: <strong>{porcentajeRestante.toFixed(2)}%</strong>
                </p>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {beneficiarios.length === 0 ? (
        <Card>
          <CardContent className="py-12">
            <div className="text-center">
              <Users className="h-12 w-12 mx-auto text-gray-300 mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">Sin beneficiarios</h3>
              <p className="text-gray-500 mb-6">Agregue beneficiarios para designar quién recibirá sus fondos</p>
            </div>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {beneficiarios.map((beneficiario) => (
            <Card key={beneficiario.id}>
              <CardContent className="p-4">
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-green-100 rounded-full">
                      <User className="h-5 w-5 text-green-600" />
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">{beneficiario.nombreCompleto}</p>
                      <p className="text-sm text-gray-500">
                        {TIPO_DOCUMENTO_LABELS[beneficiario.tipoDocumento]}: {beneficiario.numeroDocumento}
                      </p>
                    </div>
                  </div>
                  <Badge className="bg-green-100 text-green-800">
                    {beneficiario.porcentaje.toFixed(2)}%
                  </Badge>
                </div>

                <div className="mt-3 pt-3 border-t space-y-1">
                  <div className="flex items-center gap-2 text-sm text-gray-500">
                    <span className="font-medium">Parentesco:</span>
                    <span>{PARENTESCO_LABELS[beneficiario.parentesco]}</span>
                  </div>
                  {beneficiario.telefono && (
                    <div className="flex items-center gap-2 text-sm text-gray-500">
                      <Phone className="h-4 w-4" />
                      <span>{beneficiario.telefono}</span>
                    </div>
                  )}
                </div>

                <div className="mt-4 flex gap-2">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleOpenModal(beneficiario)}
                    className="flex-1"
                  >
                    <Edit2 className="h-4 w-4 mr-1" />
                    Editar
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => setShowDeleteConfirm(beneficiario.id)}
                    className="text-red-600 hover:text-red-700 hover:bg-red-50"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <Card className="w-full max-w-md mx-4">
            <CardHeader>
              <CardTitle>{editingId ? 'Editar Beneficiario' : 'Nuevo Beneficiario'}</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <label className="text-sm font-medium text-gray-700 mb-1 block">Nombre Completo</label>
                <Input
                  name="nombreCompleto"
                  value={formData.nombreCompleto}
                  onChange={handleInputChange}
                  placeholder="Ej: María Elena Pérez"
                  max={200}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-gray-700 mb-1 block">Tipo Documento</label>
                  <select
                    name="tipoDocumento"
                    value={formData.tipoDocumento}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border rounded-md"
                  >
                    <option value="CEDULA_IDENTIDAD">Cédula</option>
                    <option value="RIF">RIF</option>
                    <option value="PASAPORTE">Pasaporte</option>
                    <option value="CEDULA_EXTRANJERO">Cédula Extranjero</option>
                  </select>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-700 mb-1 block">Número Documento</label>
                  <Input
                    name="numeroDocumento"
                    value={formData.numeroDocumento}
                    onChange={handleInputChange}
                    placeholder="V-12345678"
                    max={20}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-gray-700 mb-1 block">Parentesco</label>
                  <select
                    name="parentesco"
                    value={formData.parentesco}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border rounded-md"
                  >
                    <option value="CONYUGE">Cónyuge</option>
                    <option value="HIJO">Hijo/a</option>
                    <option value="PADRE">Padre</option>
                    <option value="MADRE">Madre</option>
                    <option value="HERMANO">Hermano/a</option>
                    <option value="ABUELO">Abuelo/a</option>
                    <option value="NIETO">Nieto/a</option>
                    <option value="SOBRINO">Sobrino/a</option>
                    <option value="TIO">Tío/a</option>
                    <option value="OTRO">Otro</option>
                  </select>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-700 mb-1 block">Porcentaje</label>
                  <Input
                    name="porcentaje"
                    type="number"
                    step="0.01"
                    min="0.01"
                    max="100"
                    value={formData.porcentaje}
                    onChange={handleInputChange}
                    placeholder="50.00"
                  />
                </div>
              </div>

              <div>
                <label className="text-sm font-medium text-gray-700 mb-1 block">Teléfono (opcional)</label>
                <Input
                  name="telefono"
                  value={formData.telefono}
                  onChange={handleInputChange}
                  placeholder="+58-414-5551234"
                  max={20}
                />
              </div>

              <div className="flex gap-3 pt-4">
                <Button
                  variant="outline"
                  onClick={handleCloseModal}
                  disabled={guardando}
                  className="flex-1"
                >
                  Cancelar
                </Button>
                <Button
                  onClick={handleSave}
                  disabled={guardando}
                  className="flex-1 bg-green-600 hover:bg-green-700"
                >
                  {guardando ? (
                    <Loader2 className="h-4 w-4 animate-spin mr-2" />
                  ) : (
                    <Check className="h-4 w-4 mr-2" />
                  )}
                  {editingId ? 'Actualizar' : 'Guardar'}
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {showDeleteConfirm && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <Card className="w-full max-w-sm mx-4">
            <CardContent className="p-6 text-center">
              <AlertCircle className="h-12 w-12 mx-auto text-red-500 mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">Confirmar eliminación</h3>
              <p className="text-gray-500 mb-6">¿Está seguro de eliminar este beneficiario?</p>
              <div className="flex gap-3">
                <Button
                  variant="outline"
                  onClick={() => setShowDeleteConfirm(null)}
                  className="flex-1"
                >
                  Cancelar
                </Button>
                <Button
                  onClick={() => handleDelete(showDeleteConfirm)}
                  className="flex-1 bg-red-600 hover:bg-red-700"
                >
                  Eliminar
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}