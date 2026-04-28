'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { ArrowLeft, Download, FileText, Loader2, Calendar, Users, Receipt } from 'lucide-react';
import { toast } from 'sonner';
import { formatDate, formatCurrency } from '@/lib/utils/format';

interface Movimiento {
  id: string;
  fecha: string;
  tipo: string;
  monto: number;
  descripcion: string;
  referencia: string;
  saldoDespues: number;
}

interface EstadoCuentaData {
  socioId: string;
  socioNombre: string;
  cuentaNumero: string;
  periodo: string;
  saldoInicial: number;
  saldoFinal: number;
  totalDepositos: number;
  totalRetiros: number;
  movimientos: Movimiento[];
}

export default function AdminEstadoCuentaPage() {
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [data, setData] = useState<EstadoCuentaData | null>(null);
  const [filtros, setFiltros] = useState({
    socioId: '',
    anio: new Date().getFullYear().toString(),
    mes: (new Date().getMonth() + 1).toString().padStart(2, '0'),
  });

  const generarReporte = useCallback(async () => {
    if (!filtros.socioId.trim()) {
      toast.error('Ingrese ID de socio');
      return;
    }
    setGenerating(true);
    try {
      const params = new URLSearchParams({
        socioId: filtros.socioId,
        anio: filtros.anio,
        mes: filtros.mes,
      });
      const res = await fetch(`/api/admin/reportes/estado-cuenta?${params.toString()}`, {
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Error al generar reporte');
      const result = await res.json();
      setData(result);
      toast.success('Reporte generado exitosamente');
    } catch (err) {
      toast.error('Error al generar estado de cuenta');
    } finally {
      setGenerating(false);
    }
  }, [filtros]);

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-4">
        <Link href="/admin/reportes" className="p-2 hover:bg-gray-100 rounded-md">
          <ArrowLeft className="h-5 w-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Estado de Cuenta</h1>
          <p className="text-sm text-gray-500">Genere estados de cuenta por socio y período</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Receipt className="h-5 w-5" />
            Parámetros del Reporte
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <Label>ID Socio</Label>
              <Input
                value={filtros.socioId}
                onChange={(e) => setFiltros({ ...filtros, socioId: e.target.value })}
                placeholder="UUID del socio"
              />
            </div>
            <div>
              <Label htmlFor="filtro-anio">Año</Label>
              <select
                id="filtro-anio"
                className="w-full border rounded-md px-3 py-2"
                value={filtros.anio}
                onChange={(e) => setFiltros({ ...filtros, anio: e.target.value })}
              >
                {[2024, 2025, 2026].map((y) => (
                  <option key={y} value={y}>{y}</option>
                ))}
              </select>
            </div>
            <div>
              <Label htmlFor="filtro-mes">Mes</Label>
              <select
                id="filtro-mes"
                className="w-full border rounded-md px-3 py-2"
                value={filtros.mes}
                onChange={(e) => setFiltros({ ...filtros, mes: e.target.value })}
              >
                {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                  <option key={m} value={m.toString().padStart(2, '0')}>
                    {new Date(2024, m - 1).toLocaleDateString('es-VE', { month: 'long' })}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex items-end">
              <Button
                onClick={generarReporte}
                disabled={generating}
                className="w-full"
              >
                {generating ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : <FileText className="h-4 w-4 mr-2" />}
                Generar
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {data && (
        <>
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Resumen del Período</CardTitle>
                <Button variant="outline">
                  <Download className="h-4 w-4 mr-2" />
                  Descargar PDF
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                <div className="p-3 bg-gray-50 rounded-lg">
                  <p className="text-xs text-gray-500">Socio</p>
                  <p className="font-medium truncate">{data.socioNombre}</p>
                </div>
                <div className="p-3 bg-gray-50 rounded-lg">
                  <p className="text-xs text-gray-500">Cuenta</p>
                  <p className="font-medium">{data.cuentaNumero}</p>
                </div>
                <div className="p-3 bg-gray-50 rounded-lg">
                  <p className="text-xs text-gray-500">Período</p>
                  <p className="font-medium">{data.periodo}</p>
                </div>
                <div className="p-3 bg-gray-50 rounded-lg">
                  <p className="text-xs text-gray-500">Saldo Final</p>
                  <p className="font-medium">{formatCurrency(data.saldoFinal)}</p>
                </div>
                <div className="p-3 bg-gray-50 rounded-lg">
                  <p className="text-xs text-gray-500">Movimientos</p>
                  <p className="font-medium">{data.movimientos.length}</p>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-4 mt-4">
                <div className="p-3 bg-green-50 rounded-lg border border-green-200">
                  <p className="text-xs text-green-600">Total Depósitos</p>
                  <p className="text-lg font-bold text-green-700">{formatCurrency(data.totalDepositos)}</p>
                </div>
                <div className="p-3 bg-red-50 rounded-lg border border-red-200">
                  <p className="text-xs text-red-600">Total Retiros</p>
                  <p className="text-lg font-bold text-red-700">{formatCurrency(data.totalRetiros)}</p>
                </div>
                <div className="p-3 bg-blue-50 rounded-lg border border-blue-200">
                  <p className="text-xs text-blue-600">Saldo Anterior</p>
                  <p className="text-lg font-bold text-blue-700">{formatCurrency(data.saldoInicial)}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Detalle de Movimientos</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b bg-gray-50">
                      <th className="p-3 text-left">Fecha</th>
                      <th className="p-3 text-left">Tipo</th>
                      <th className="p-3 text-left">Descripción</th>
                      <th className="p-3 text-right">Monto</th>
                      <th className="p-3 text-left">Referencia</th>
                      <th className="p-3 text-right">Saldo</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.movimientos.map((mov) => (
                      <tr key={mov.id} className="border-b hover:bg-gray-50">
                        <td className="p-3">{formatDate(mov.fecha)}</td>
                        <td className="p-3">
                          <Badge className={mov.tipo === 'DEPOSITO' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}>
                            {mov.tipo}
                          </Badge>
                        </td>
                        <td className="p-3">{mov.descripcion}</td>
                        <td className={`p-3 text-right font-medium ${mov.tipo === 'DEPOSITO' ? 'text-green-600' : 'text-red-600'}`}>
                          {mov.tipo === 'DEPOSITO' ? '+' : '-'}{formatCurrency(mov.monto)}
                        </td>
                        <td className="p-3 text-xs text-gray-500">{mov.referencia}</td>
                        <td className="p-3 text-right">{formatCurrency(mov.saldoDespues)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
}