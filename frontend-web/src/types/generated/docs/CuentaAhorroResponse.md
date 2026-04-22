
# CuentaAhorroResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`numeroCuenta` | string
`socioId` | string
`saldoActual` | number
`saldoRetenido` | number
`saldoDisponible` | number
`tasaInteres` | number
`montoMinimoRequerido` | number
`estado` | string
`tipoCuenta` | string
`moneda` | string
`fechaApertura` | Date
`fechaUltimaOperacion` | Date

## Example

```typescript
import type { CuentaAhorroResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "numeroCuenta": null,
  "socioId": null,
  "saldoActual": null,
  "saldoRetenido": null,
  "saldoDisponible": null,
  "tasaInteres": null,
  "montoMinimoRequerido": null,
  "estado": null,
  "tipoCuenta": null,
  "moneda": null,
  "fechaApertura": null,
  "fechaUltimaOperacion": null,
} satisfies CuentaAhorroResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as CuentaAhorroResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


