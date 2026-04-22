
# ResultadoCuenta


## Properties

Name | Type
------------ | -------------
`cuentaId` | string
`numeroCuenta` | string
`exitoso` | boolean
`rendimientoId` | string
`error` | string

## Example

```typescript
import type { ResultadoCuenta } from ''

// TODO: Update the object below with actual values
const example = {
  "cuentaId": null,
  "numeroCuenta": null,
  "exitoso": null,
  "rendimientoId": null,
  "error": null,
} satisfies ResultadoCuenta

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as ResultadoCuenta
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


