import React from 'react';
import { AccountCard } from '@/components/ui/account-card';
import { Car, AlertTriangle } from 'lucide-react';

export default function DashboardPage() {
  // Simulación de datos para la vista del transportista
  const tasaBcvHoy = 50.20;
  
  return (
    <div className="p-8 bg-gray-50 min-h-screen">
      {/* Header Personalizado */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Hola, Carlos 👋</h1>
        <p className="text-gray-500 mt-1">Lunes, 27 Abr 2026</p>
      </div>

      {/* Grid Principal */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Columna Izquierda: Cuentas (Ocupa 2 espacios en LG) */}
        <div className="lg:col-span-2 flex flex-col gap-6">
          <h2 className="text-xl font-semibold text-[#1A3C6E] flex items-center gap-2">
            Tus Cuentas
          </h2>
          
          <div className="flex flex-wrap gap-6">
            <AccountCard 
              numeroCuenta="01340001000000005678"
              saldoVes={12450000.50}
              tasaBcv={tasaBcvHoy}
              estado="ACTIVA"
              tipo="CORRIENTE"
            />
            
            <AccountCard 
              numeroCuenta="01340001000000009999"
              saldoVes={5000000.00}
              tasaBcv={tasaBcvHoy}
              estado="ACTIVA"
              tipo="EMERGENCIA"
            />
          </div>
        </div>

        {/* Columna Derecha: Transporte / Unidad */}
        <div className="flex flex-col gap-6">
          <h2 className="text-xl font-semibold text-[#1A3C6E] flex items-center gap-2">
            <Car size={24} />
            Tu Unidad
          </h2>

          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
            <div className="flex justify-between items-start mb-4">
              <div>
                <p className="text-xs text-gray-500 uppercase tracking-wider font-bold">Encava Ent-610</p>
                <h3 className="text-xl font-mono font-bold text-gray-800">AA11BB2</h3>
              </div>
              <span className="bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded font-medium">Buseta</span>
            </div>

            <div className="space-y-3">
              <div className="flex justify-between items-center text-sm">
                <span className="text-gray-500">Año</span>
                <span className="font-medium text-gray-900">2012</span>
              </div>
              <div className="flex justify-between items-center text-sm">
                <span className="text-gray-500">Ruta</span>
                <span className="font-medium text-gray-900">Caracas - Guarenas</span>
              </div>
            </div>

            {/* Alerta de Vencimiento de SOAT (Simulando el Epic de Transporte) */}
            <div className="mt-5 p-3 bg-orange-50 border border-orange-200 rounded-lg flex gap-3">
              <AlertTriangle className="text-orange-500 shrink-0" size={20} />
              <div>
                <p className="text-sm font-semibold text-orange-800">El Seguro SOAT vence en 15 días</p>
                <p className="text-xs text-orange-600 mt-1">Tienes fondos suficientes en tu cuenta de Emergencia para renovarlo.</p>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  );
}
