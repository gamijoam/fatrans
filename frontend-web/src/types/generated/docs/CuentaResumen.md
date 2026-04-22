
# CuentaResumen


## Properties

Name | Type
------------ | -------------
`id` | string
`numeroCuenta` | string
`saldoActual` | number
`estado` | string
`tipoCuenta` | string
`fechaApertura` | Date

## Example

```typescript
import type { CuentaResumen } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "numeroCuenta": null,
  "saldoActual": null,
  "estado": null,
  "tipoCuenta": null,
  "fechaApertura": null,
} satisfies CuentaResumen

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as CuentaResumen
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


