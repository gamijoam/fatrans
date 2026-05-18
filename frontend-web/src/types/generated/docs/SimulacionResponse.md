
# SimulacionResponse


## Properties

Name | Type
------------ | -------------
`monto` | number
`plazoMeses` | number
`tasaInteresAnual` | number
`cuotaMensual` | number
`totalIntereses` | number
`totalAPagar` | number
`planSimulado` | [Array&lt;CuotaSimulada&gt;](CuotaSimulada.md)
`nota` | string

## Example

```typescript
import type { SimulacionResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "monto": null,
  "plazoMeses": null,
  "tasaInteresAnual": null,
  "cuotaMensual": null,
  "totalIntereses": null,
  "totalAPagar": null,
  "planSimulado": null,
  "nota": null,
} satisfies SimulacionResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as SimulacionResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


