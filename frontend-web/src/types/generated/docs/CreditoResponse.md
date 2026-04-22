
# CreditoResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`numeroSolicitud` | string
`socioId` | string
`tipoCredito` | [TipoCreditoResponse](TipoCreditoResponse.md)
`montoSolicitado` | number
`plazoMeses` | number
`tasaInteresAplicada` | number
`estado` | string
`colateralMontoRetenido` | number
`referenciaDesembolso` | string
`fechaDesembolso` | Date
`plan` | [PlanAmortizacionResponse](PlanAmortizacionResponse.md)
`resumen` | [ResumenResponse](ResumenResponse.md)

## Example

```typescript
import type { CreditoResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "numeroSolicitud": null,
  "socioId": null,
  "tipoCredito": null,
  "montoSolicitado": null,
  "plazoMeses": null,
  "tasaInteresAplicada": null,
  "estado": null,
  "colateralMontoRetenido": null,
  "referenciaDesembolso": null,
  "fechaDesembolso": null,
  "plan": null,
  "resumen": null,
} satisfies CreditoResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as CreditoResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


