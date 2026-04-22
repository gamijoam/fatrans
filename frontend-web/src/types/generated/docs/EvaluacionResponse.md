
# EvaluacionResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`solicitudId` | string
`socioId` | string
`puntajeAntiguedad` | number
`puntajeHistorialAhorro` | number
`puntajeCapacidadPago` | number
`scoreInterno` | number
`scoreHash` | string
`elegible` | boolean
`nivelRiesgo` | string
`tasaInteresFinal` | number
`mensajeDecision` | string
`evaluador` | string

## Example

```typescript
import type { EvaluacionResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "solicitudId": null,
  "socioId": null,
  "puntajeAntiguedad": null,
  "puntajeHistorialAhorro": null,
  "puntajeCapacidadPago": null,
  "scoreInterno": null,
  "scoreHash": null,
  "elegible": null,
  "nivelRiesgo": null,
  "tasaInteresFinal": null,
  "mensajeDecision": null,
  "evaluador": null,
} satisfies EvaluacionResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as EvaluacionResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


