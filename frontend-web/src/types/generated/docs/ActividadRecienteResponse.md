
# ActividadRecienteResponse


## Properties

Name | Type
------------ | -------------
`nuevosSociosMes` | number
`depositosMes` | number
`retirosMes` | number
`prestamosAprobadosMes` | number
`prestamosDesembolsadosMes` | number
`montoDepositadoMes` | number
`montoRetiradoMes` | number

## Example

```typescript
import type { ActividadRecienteResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "nuevosSociosMes": null,
  "depositosMes": null,
  "retirosMes": null,
  "prestamosAprobadosMes": null,
  "prestamosDesembolsadosMes": null,
  "montoDepositadoMes": null,
  "montoRetiradoMes": null,
} satisfies ActividadRecienteResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as ActividadRecienteResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


