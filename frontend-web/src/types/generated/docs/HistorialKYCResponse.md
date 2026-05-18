
# HistorialKYCResponse


## Properties

Name | Type
------------ | -------------
`socioId` | string
`totalVerificaciones` | number
`historial` | [Array&lt;HistorialItemResponse&gt;](HistorialItemResponse.md)

## Example

```typescript
import type { HistorialKYCResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "socioId": null,
  "totalVerificaciones": null,
  "historial": null,
} satisfies HistorialKYCResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as HistorialKYCResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


