
# PagoCuotaRequest


## Properties

Name | Type
------------ | -------------
`monto` | number
`referenciaPago` | string
`canalOrigen` | string

## Example

```typescript
import type { PagoCuotaRequest } from ''

// TODO: Update the object below with actual values
const example = {
  "monto": null,
  "referenciaPago": null,
  "canalOrigen": null,
} satisfies PagoCuotaRequest

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as PagoCuotaRequest
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


