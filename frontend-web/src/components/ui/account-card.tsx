import React from 'react';
import { Wallet, RefreshCw, AlertCircle } from 'lucide-react';

interface AccountCardProps {
  numeroCuenta: string;
  saldoVes: number;
  tasaBcv: number;
  estado: 'ACTIVA' | 'BLOQUEADA' | 'INACTIVA';
  tipo: 'CORRIENTE' | 'PROGRAMADO' | 'EMERGENCIA';
}

export function AccountCard({ numeroCuenta, saldoVes, tasaBcv, estado, tipo }: AccountCardProps) {
  const saldoUsd = tasaBcv > 0 ? (saldoVes / tasaBcv).toFixed(2) : '0.00';

  const formatVes = (monto: number) => {
    return new Intl.NumberFormat('es-VE', {
      style: 'currency',
      currency: 'VES',
      minimumFractionDigits: 2,
    }).format(monto);
  };

  const getStatusBadge = () => {
    switch (estado) {
      case 'ACTIVA':
        return <span className="bg-[#2E7D32] text-white text-xs px-2 py-1 rounded-full font-medium">ACTIVA</span>;
      case 'BLOQUEADA':
        return <span className="bg-[#B71C1C] text-white text-xs px-2 py-1 rounded-full font-medium flex items-center gap-1"><AlertCircle size={12}/> BLOQUEADA</span>;
      default:
        return <span className="bg-gray-400 text-white text-xs px-2 py-1 rounded-full font-medium">INACTIVA</span>;
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-5 flex flex-col gap-4 min-w-[280px] w-full max-w-md">
      {/* Header */}
      <div className="flex justify-between items-center border-b border-gray-50 pb-3">
        <div className="flex items-center gap-2">
          <div className="p-2 bg-[#1A3C6E]/10 rounded-lg text-[#1A3C6E]">
            <Wallet size={20} />
          </div>
          <div>
            <h3 className="text-sm font-semibold text-gray-700">Ahorro {tipo}</h3>
            <p className="text-xs font-mono text-gray-400">•••• {numeroCuenta.slice(-4)}</p>
          </div>
        </div>
        <div>{getStatusBadge()}</div>
      </div>

      {/* Balances */}
      <div className="flex flex-col gap-1">
        <p className="text-xs text-gray-500 font-medium uppercase tracking-wider">Saldo Disponible</p>
        <h2 className="text-3xl font-bold text-[#2E7D32]">
          {formatVes(saldoVes)}
        </h2>
        
        {/* USD Equivalent */}
        <div className="flex items-center gap-2 mt-1 bg-gray-50 p-2 rounded-md">
          <RefreshCw size={14} className="text-gray-400" />
          <span className="text-sm font-medium text-gray-600">
            ≈ ${saldoUsd} USD
          </span>
          <span className="text-[10px] text-gray-400 ml-auto">
            Tasa: Bs {tasaBcv.toFixed(2)}
          </span>
        </div>
      </div>
    </div>
  );
}
