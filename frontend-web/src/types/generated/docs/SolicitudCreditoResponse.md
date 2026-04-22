
# SolicitudCreditoResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`numeroSolicitud` | string
`socioId` | string
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
`evaluacion` | [EvaluacionResponse](EvaluacionResponse.md)
`createdAt` | Date
`fechaAprobacion` | Date
`fechaRechazo` | Date
`mensaje` | string

## Example

```typescript
import type { SolicitudCreditoResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "numeroSolicitud": null,
  "socioId": null,
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
  "evaluacion": null,
  "createdAt": null,
  "fechaAprobacion": null,
  "fechaRechazo": null,
  "mensaje": null,
} satisfies SolicitudCreditoResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as SolicitudCreditoResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


