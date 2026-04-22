
# MetricasResponse


## Properties

Name | Type
------------ | -------------
`tiempoPromedioRevisionHoras` | number
`tasaAprobacion` | number
`tasaRechazo` | number
`kycPorExpirarProximoMes` | number

## Example

```typescript
import type { MetricasResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "tiempoPromedioRevisionHoras": null,
  "tasaAprobacion": null,
  "tasaRechazo": null,
  "kycPorExpirarProximoMes": null,
} satisfies MetricasResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as MetricasResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


