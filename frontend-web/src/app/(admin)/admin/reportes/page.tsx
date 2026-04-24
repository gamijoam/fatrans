'use client';

import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { FileText, Users, CreditCard, Receipt, BarChart3, Download, Filter } from 'lucide-react';

interface ReporteItem {
  id: string;
  titulo: string;
  descripcion: string;
  icono: React.ReactNode;
  href: string;
  color: string;
}

const REPORTES: ReporteItem[] = [
  {
    id: 'socios',
    titulo: 'Reporte de Socios',
    descripcion: 'Lista completa de socios activos e inactivos con sus datos de contacto y estado',
    icono: <Users className="h-6 w-6" />,
    href: '/admin/reportes/socios',
    color: 'border-l-blue-500',
  },
  {
    id: 'creditos',
    titulo: 'Reporte de Créditos',
    descripcion: 'Estado de solicitudes y créditos por estado, montos y plazos',
    icono: <CreditCard className="h-6 w-6" />,
    href: '/admin/reportes/creditos',
    color: 'border-l-green-500',
  },
  {
    id: 'estado-cuenta',
    titulo: 'Estados de Cuenta',
    descripcion: 'Movimientos y saldos por cuenta con historial de transacciones',
    icono: <Receipt className="h-6 w-6" />,
    href: '/admin/reportes/estado-cuenta',
    color: 'border-l-purple-500',
  },
];

export default function AdminReportesPage() {
  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Reportes y Estadísticas</h1>
          <p className="text-sm text-gray-500">Genere y descargue reportes del sistema</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <BarChart3 className="h-5 w-5" />
            Menú de Reportes
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {REPORTES.map((reporte) => (
              <Link key={reporte.id} href={reporte.href}>
                <Card className={`cursor-pointer hover:shadow-lg transition-shadow border-l-4 ${reporte.color}`}>
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm font-medium flex items-center gap-2">
                      <span className="text-gray-400">{reporte.icono}</span>
                      {reporte.titulo}
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-xs text-gray-500">{reporte.descripcion}</p>
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Download className="h-5 w-5" />
            Formatos de Descarga
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap gap-4">
            <div className="flex items-center gap-2 px-4 py-2 bg-gray-100 rounded-lg">
              <FileText className="h-4 w-4 text-gray-600" />
              <span className="text-sm font-medium">PDF</span>
            </div>
            <div className="flex items-center gap-2 px-4 py-2 bg-gray-100 rounded-lg">
              <FileText className="h-4 w-4 text-gray-600" />
              <span className="text-sm font-medium">Excel (XLSX)</span>
            </div>
            <div className="flex items-center gap-2 px-4 py-2 bg-gray-100 rounded-lg">
              <FileText className="h-4 w-4 text-gray-600" />
              <span className="text-sm font-medium">CSV</span>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Filter className="h-5 w-5" />
            Filtros Comunes
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="text-sm font-medium text-gray-700 block mb-1">Rango de Fechas</label>
              <select className="w-full border rounded-md px-3 py-2 text-sm">
                <option>Último mes</option>
                <option>Último trimestre</option>
                <option>Últimos 6 meses</option>
                <option>Último año</option>
                <option>Personalizado</option>
              </select>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-700 block mb-1">Estado</label>
              <select className="w-full border rounded-md px-3 py-2 text-sm">
                <option>Todos</option>
                <option>Activos</option>
                <option>Inactivos</option>
                <option>Pendientes</option>
              </select>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-700 block mb-1">Formato</label>
              <select className="w-full border rounded-md px-3 py-2 text-sm">
                <option>PDF</option>
                <option>Excel (XLSX)</option>
                <option>CSV</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}