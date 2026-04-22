
# ResumenResponse


## Properties

Name | Type
------------ | -------------
`cuotasPagadas` | number
`cuotasPendientes` | number
`cuotasVencidas` | number
`totalIntereses` | number
`totalPagadoIntereses` | number

## Example

```typescript
import type { ResumenResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "cuotasPagadas": null,
  "cuotasPendientes": null,
  "cuotasVencidas": null,
  "totalIntereses": null,
  "totalPagadoIntereses": null,
} satisfies ResumenResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as ResumenResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


