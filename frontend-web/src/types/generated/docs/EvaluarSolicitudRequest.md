
# EvaluarSolicitudRequest


## Properties

Name | Type
------------ | -------------
`puntajeAntiguedad` | number
`puntajeHistorialAhorro` | number
`puntajeCapacidadPago` | number
`salarioEstimado` | number

## Example

```typescript
import type { EvaluarSolicitudRequest } from ''

// TODO: Update the object below with actual values
const example = {
  "puntajeAntiguedad": null,
  "puntajeHistorialAhorro": null,
  "puntajeCapacidadPago": null,
  "salarioEstimado": null,
} satisfies EvaluarSolicitudRequest

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as EvaluarSolicitudRequest
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


