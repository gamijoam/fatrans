
# DashboardEstadisticasResponse


## Properties

Name | Type
------------ | -------------
`totalSocios` | number
`sociosActivos` | number
`sociosInactivos` | number
`sociosPendientes` | number
`totalAportaciones` | number
`totalCuentasAhorro` | number
`cuentasActivas` | number
`cuentasSuspendidas` | number
`depositosMes` | number
`retirosMes` | number
`prestamosActivos` | number
`solicitudesPendientes` | number
`solicitudesAprobadas` | number
`solicitudesRechazadas` | number
`capitalDesembolsado` | number
`carteraVencida` | number
`cuotasVencidas` | number
`cuotasEnMora` | number
`cuotasPagadas` | number
`interesesMoraGenerados` | number
`tasaCumplimiento` | number
`tasaMora` | number
`rendimientoPromedio` | number
`actividadReciente` | [ActividadRecienteResponse](ActividadRecienteResponse.md)

## Example

```typescript
import type { DashboardEstadisticasResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "totalSocios": null,
  "sociosActivos": null,
  "sociosInactivos": null,
  "sociosPendientes": null,
  "totalAportaciones": null,
  "totalCuentasAhorro": null,
  "cuentasActivas": null,
  "cuentasSuspendidas": null,
  "depositosMes": null,
  "retirosMes": null,
  "prestamosActivos": null,
  "solicitudesPendientes": null,
  "solicitudesAprobadas": null,
  "solicitudesRechazadas": null,
  "capitalDesembolsado": null,
  "carteraVencida": null,
  "cuotasVencidas": null,
  "cuotasEnMora": null,
  "cuotasPagadas": null,
  "interesesMoraGenerados": null,
  "tasaCumplimiento": null,
  "tasaMora": null,
  "rendimientoPromedio": null,
  "actividadReciente": null,
} satisfies DashboardEstadisticasResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as DashboardEstadisticasResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


