
# PlanAmortizacionResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`solicitudId` | string
`montoPrincipal` | number
`tasaInteres` | number
`plazoMeses` | number
`frecuenciaPago` | string
`fechaInicio` | Date
`fechaFin` | Date
`numeroCuotas` | number
`cuotaMensual` | number
`totalIntereses` | number
`totalPagado` | number
`saldoPendiente` | number
`estado` | string
`cuotas` | [Array&lt;CuotaResponse&gt;](CuotaResponse.md)

## Example

```typescript
import type { PlanAmortizacionResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "solicitudId": null,
  "montoPrincipal": null,
  "tasaInteres": null,
  "plazoMeses": null,
  "frecuenciaPago": null,
  "fechaInicio": null,
  "fechaFin": null,
  "numeroCuotas": null,
  "cuotaMensual": null,
  "totalIntereses": null,
  "totalPagado": null,
  "saldoPendiente": null,
  "estado": null,
  "cuotas": null,
} satisfies PlanAmortizacionResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as PlanAmortizacionResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


