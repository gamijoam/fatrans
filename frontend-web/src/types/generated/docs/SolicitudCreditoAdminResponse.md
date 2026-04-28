
# SolicitudCreditoAdminResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`numeroSolicitud` | string
`socioId` | string
`socioNombre` | string
`socioNumero` | string
`socioCedula` | string
`socioCorreo` | string
`socioEmpresa` | string
`tipoCreditoId` | number
`tipoCreditoNombre` | string
`montoSolicitado` | number
`plazoMeses` | number
`tasaInteresAplicada` | number
`cuotaMensualEstimada` | number
`estado` | string
`colateralCuentaId` | string
`colateralMontoRetenido` | number
`destinoCredito` | string
`createdAt` | Date
`fechaAprobacion` | Date
`fechaRechazo` | Date
`motivoRechazo` | string

## Example

```typescript
import type { SolicitudCreditoAdminResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "numeroSolicitud": null,
  "socioId": null,
  "socioNombre": null,
  "socioNumero": null,
  "socioCedula": null,
  "socioCorreo": null,
  "socioEmpresa": null,
  "tipoCreditoId": null,
  "tipoCreditoNombre": null,
  "montoSolicitado": null,
  "plazoMeses": null,
  "tasaInteresAplicada": null,
  "cuotaMensualEstimada": null,
  "estado": null,
  "colateralCuentaId": null,
  "colateralMontoRetenido": null,
  "destinoCredito": null,
  "createdAt": null,
  "fechaAprobacion": null,
  "fechaRechazo": null,
  "motivoRechazo": null,
} satisfies SolicitudCreditoAdminResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as SolicitudCreditoAdminResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


