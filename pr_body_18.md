## Resumen
Implementa Issue #18 - Módulo de Créditos frontend (tipos, solicitudes, cuotas, pagos)

## Cambios

### Frontend

#### creditosApi (client.ts)
- `getTiposCredito()` - Lista tipos de crédito
- `getSolicitudesPorSocio(socioId)` - Solicitudes del socio
- `getSolicitud(numero)` - Detalle de solicitud
- `crearSolicitud(data)` - Nueva solicitud
- `getPlan(solicitudNumero)` - Plan de amortización
- `getCuotas(creditoNumero)` - Estado de cuotas
- `simular(data)` - Simular crédito

#### Pages
- `/dashboard/creditos` - Lista solicitudes y tipos disponibles
- `/dashboard/creditos/simulador` - Calculadora de crédito con plan
- `/dashboard/creditos/[numero]` - Detalle de solicitud con plan

#### API Proxy Routes
- `GET /api/creditos/tipos-credito` - Lista tipos
- `GET /api/creditos/solicitudes/socio/[socioId]` - Solicitudes
- `GET /api/creditos/solicitudes/[numero]` - Detalle
- `POST /api/creditos/simulador` - Simular

### Seguridad (Auditoría corregida)
- ✅ IDOR fix: proxy routes validan socioId del token vs params
- ✅ getSocioIdFromToken() extrae socio_id del JWT
- ✅ Validación monto/plazo > 0
- ✅ Límites max: monto 10M, plazo 360 meses

## Criterios de aceptación
- [x] Lista de solicitudes del socio
- [x] Detalle con información completa
- [x] Simulador con plan de amortización
- [x] Estado y timeline del crédito

## Labels
`frontend`, `module:creditos`, `security`

## Milestone
Sprint 4: Dashboard Socio

Closes #18