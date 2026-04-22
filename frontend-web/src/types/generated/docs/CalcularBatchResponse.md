
# CalcularBatchResponse


## Properties

Name | Type
------------ | -------------
`totalCuentas` | number
`procesadas` | number
`exitosas` | number
`fallidas` | number
`resultados` | [Array&lt;ResultadoCuenta&gt;](ResultadoCuenta.md)
`fechaProcesamiento` | Date

## Example

```typescript
import type { CalcularBatchResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "totalCuentas": null,
  "procesadas": null,
  "exitosas": null,
  "fallidas": null,
  "resultados": null,
  "fechaProcesamiento": null,
} satisfies CalcularBatchResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as CalcularBatchResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


