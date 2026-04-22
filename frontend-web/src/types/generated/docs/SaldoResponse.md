
# SaldoResponse


## Properties

Name | Type
------------ | -------------
`numeroCuenta` | string
`saldoActual` | number
`saldoRetenido` | number
`saldoDisponible` | number
`fechaConsulta` | Date
`limiteDeposito` | number
`limiteRetiroDiario` | number
`retirosRealizadosHoy` | number
`retirosRestantesHoy` | number

## Example

```typescript
import type { SaldoResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "numeroCuenta": null,
  "saldoActual": null,
  "saldoRetenido": null,
  "saldoDisponible": null,
  "fechaConsulta": null,
  "limiteDeposito": null,
  "limiteRetiroDiario": null,
  "retirosRealizadosHoy": null,
  "retirosRestantesHoy": null,
} satisfies SaldoResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as SaldoResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


