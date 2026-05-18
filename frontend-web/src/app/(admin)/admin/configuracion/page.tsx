'use client';

import { useEffect, useState, useCallback } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Loader2, Settings, Lock, Unlock, AlertCircle, CheckCircle } from 'lucide-react';
import { toast } from 'sonner';

interface Parametro {
  key: string;
  valor: string;
  tipo: string;
  descripcion: string;
  categoria: string;
  editable: boolean;
  fechaActualizacion: string | null;
  actualizadoPor: string | null;
}

const CATEGORIAS = ['TASA', 'LIMITE', 'COMISION', 'CUENTA', 'KYC', 'SISTEMA'];

const TIPO_LABELS: Record<string, string> = {
  STRING: 'Texto',
  NUMERIC: 'Numérico',
  BOOLEAN: 'Sí/No',
  DATE: 'Fecha',
  PERCENTAGE: 'Porcentaje',
  CURRENCY: 'Moneda',
};

export default function AdminParametrosPage() {
  const [parametros, setParametros] = useState<Parametro[]>([]);
  const [loading, setLoading] = useState(true);
  const [categoriaFiltro, setCategoriaFiltro] = useState<string>('');
  const [editandoKey, setEditandoKey] = useState<string | null>(null);
  const [editandoValor, setEditandoValor] = useState<string>('');
  const [guardando, setGuardando] = useState(false);

  const cargarParametros = useCallback(async () => {
    setLoading(true);
    try {
      const params = categoriaFiltro ? `?categoria=${categoriaFiltro}` : '';
      const res = await fetch(`/api/admin/parametros${params}`, {
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al cargar parámetros');
      const data = await res.json();
      setParametros(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Error cargando parámetros:', err);
      toast.error('Error al cargar parámetros');
    } finally {
      setLoading(false);
    }
  }, [categoriaFiltro]);

  useEffect(() => {
    cargarParametros();
  }, [cargarParametros]);

  const handleEditar = (param: Parametro) => {
    setEditandoKey(param.key);
    setEditandoValor(param.valor);
  };

  const handleCancelar = () => {
    setEditandoKey(null);
    setEditandoValor('');
  };

  const handleGuardar = async (key: string) => {
    setGuardando(true);
    try {
      const res = await fetch('/api/admin/parametros', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ key, valor: editandoValor }),
      });
      if (!res.ok) throw new Error('Error al guardar');
      toast.success('Parámetro actualizado correctamente');
      setEditandoKey(null);
      cargarParametros();
    } catch (err) {
      console.error('Error guardando:', err);
      toast.error('Error al guardar parámetro');
    } finally {
      setGuardando(false);
    }
  };

  const groupedParams = parametros.reduce((acc, param) => {
    if (!acc[param.categoria]) acc[param.categoria] = [];
    acc[param.categoria].push(param);
    return acc;
  }, {} as Record<string, Parametro[]>);

  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return '-';
    try {
      return new Date(dateStr).toLocaleString('es-VE');
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Parámetros del Sistema</h1>
        <Badge variant="outline" className="text-primary">
          {parametros.length} parámetros
        </Badge>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <Settings className="h-5 w-5" />
              Configuración General
            </CardTitle>
            <div className="flex gap-2">
              <select
                value={categoriaFiltro}
                onChange={(e) => setCategoriaFiltro(e.target.value)}
                className="px-3 py-2 border rounded-md text-sm"
              >
                <option value="">Todas las categorías</option>
                {CATEGORIAS.map((cat) => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-green-600" />
            </div>
          ) : parametros.length === 0 ? (
            <div className="text-center py-12">
              <AlertCircle className="h-12 w-12 mx-auto text-gray-300 mb-3" />
              <p className="text-gray-500">No hay parámetros configurados</p>
            </div>
          ) : (
            <div className="space-y-6">
              {Object.entries(groupedParams).map(([categoria, params]) => (
                <div key={categoria}>
                  <h3 className="text-lg font-semibold text-gray-700 mb-3 flex items-center gap-2">
                    <Settings className="h-4 w-4" />
                    {categoria}
                  </h3>
                  <div className="space-y-2">
                    {params.map((param) => (
                      <div
                        key={param.key}
                        className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50"
                      >
                        <div className="flex-1">
                          <div className="flex items-center gap-2">
                            <p className="font-mono font-medium text-sm">{param.key}</p>
                            {param.editable ? (
                              <Unlock className="h-4 w-4 text-green-500" />
                            ) : (
                              <Lock className="h-4 w-4 text-red-500" />
                            )}
                            <Badge variant="outline" className="text-xs">
                              {TIPO_LABELS[param.tipo] || param.tipo}
                            </Badge>
                          </div>
                          <p className="text-sm text-gray-500 mt-1">{param.descripcion}</p>
                          {param.fechaActualizacion && (
                            <p className="text-xs text-gray-400 mt-1">
                              Actualizado: {formatDate(param.fechaActualizacion)}
                            </p>
                          )}
                        </div>
                        <div className="flex items-center gap-2 ml-4">
                          {editandoKey === param.key ? (
                            <>
                              <Input
                                value={editandoValor}
                                onChange={(e) => setEditandoValor(e.target.value)}
                                className="w-32"
                                disabled={!param.editable || guardando}
                              />
                              <Button
                                size="sm"
                                onClick={() => handleGuardar(param.key)}
                                disabled={guardando}
                              >
                                <CheckCircle className="h-4 w-4 mr-1" />
                                Guardar
                              </Button>
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={handleCancelar}
                                disabled={guardando}
                              >
                                Cancelar
                              </Button>
                            </>
                          ) : (
                            <>
                              <span className="font-mono text-sm bg-gray-100 px-3 py-1 rounded">
                                {param.valor}
                              </span>
                              {param.editable && (
                                <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={() => handleEditar(param)}
                                >
                                  Editar
                                </Button>
                              )}
                            </>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}