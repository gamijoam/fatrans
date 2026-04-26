import { NextRequest, NextResponse } from 'next/server';

interface Cuota {
  numero: number;
  fechaPago: string;
  cuotaMensual: number;
  capital: number;
  interes: number;
  saldoRestante: number;
}

interface SimulacionResponse {
  monto: number;
  plazoMeses: number;
  tasaAnual: number;
  cuotaMensual: number;
  totalPagar: number;
  totalInteres: number;
  tablaAmortizacion: Cuota[];
}

export async function POST(request: NextRequest) {
  try {
    const accessToken = request.cookies.get('access_token')?.value;
    if (!accessToken) {
      return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
    }

    const body = await request.json();
    const { monto, plazoMeses, tasaAnual } = body;

    if (!monto || monto <= 0) {
      return NextResponse.json({ message: 'Monto inválido' }, { status: 400 });
    }

    if (!plazoMeses || plazoMeses <= 0 || plazoMeses > 120) {
      return NextResponse.json({ message: 'Plazo inválido (1-120 meses)' }, { status: 400 });
    }

    if (!tasaAnual || tasaAnual <= 0 || tasaAnual > 100) {
      return NextResponse.json({ message: 'Tasa inválida' }, { status: 400 });
    }

    const tasaMensual = tasaAnual / 100 / 12;
    const numCuotas = plazoMeses;

    const cuotaMensual = monto * (tasaMensual * Math.pow(1 + tasaMensual, numCuotas)) /
                          (Math.pow(1 + tasaMensual, numCuotas) - 1);

    const tablaAmortizacion: Cuota[] = [];
    let saldoRestante = monto;

    for (let i = 1; i <= numCuotas; i++) {
      const interes = saldoRestante * tasaMensual;
      const capital = cuotaMensual - interes;
      saldoRestante = Math.max(0, saldoRestante - capital);

      const fechaPago = new Date();
      fechaPago.setMonth(fechaPago.getMonth() + i);

      tablaAmortizacion.push({
        numero: i,
        fechaPago: fechaPago.toISOString(),
        cuotaMensual: Math.round(cuotaMensual * 100) / 100,
        capital: Math.round(capital * 100) / 100,
        interes: Math.round(interes * 100) / 100,
        saldoRestante: Math.round(saldoRestante * 100) / 100,
      });
    }

    const totalPagar = cuotaMensual * numCuotas;
    const totalInteres = totalPagar - monto;

    const response: SimulacionResponse = {
      monto,
      plazoMeses,
      tasaAnual,
      cuotaMensual: Math.round(cuotaMensual * 100) / 100,
      totalPagar: Math.round(totalPagar * 100) / 100,
      totalInteres: Math.round(totalInteres * 100) / 100,
      tablaAmortizacion,
    };

    return NextResponse.json(response, { status: 200 });

  } catch (error) {
    console.error('Simulador error:', error);
    return NextResponse.json(
      { message: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}