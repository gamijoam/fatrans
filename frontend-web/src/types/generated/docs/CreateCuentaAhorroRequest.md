
# CreateCuentaAhorroRequest


## Properties

Name | Type
------------ | -------------
`socioId` | string
`tipoCuenta` | string
`moneda` | string
`montoMinimoRequerido` | number
`tasaInteres` | number

## Example

```typescript
import type { CreateCuentaAhorroRequest } from ''

// TODO: Update the object below with actual values
const example = {
  "socioId": null,
  "tipoCuenta": null,
  "moneda": null,
  "montoMinimoRequerido": null,
  "tasaInteres": null,
} satisfies CreateCuentaAhorroRequest

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as CreateCuentaAhorroRequest
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


