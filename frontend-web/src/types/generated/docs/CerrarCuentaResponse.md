
# CerrarCuentaResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`numeroCuenta` | string
`estado` | string
`fechaCierre` | Date
`saldoFinal` | number
`mensaje` | string

## Example

```typescript
import type { CerrarCuentaResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "numeroCuenta": null,
  "estado": null,
  "fechaCierre": null,
  "saldoFinal": null,
  "mensaje": null,
} satisfies CerrarCuentaResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as CerrarCuentaResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


