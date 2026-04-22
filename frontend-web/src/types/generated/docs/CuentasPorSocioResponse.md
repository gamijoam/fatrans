
# CuentasPorSocioResponse


## Properties

Name | Type
------------ | -------------
`socioId` | string
`totalCuentas` | number
`cuentas` | [Array&lt;CuentaResumen&gt;](CuentaResumen.md)

## Example

```typescript
import type { CuentasPorSocioResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "socioId": null,
  "totalCuentas": null,
  "cuentas": null,
} satisfies CuentasPorSocioResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as CuentasPorSocioResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


