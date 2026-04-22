
# CuotaResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`numeroCuota` | number
`fechaVencimiento` | Date
`fechaPago` | Date
`capital` | number
`interes` | number
`montoCuota` | number
`saldoInsoluto` | number
`estado` | string
`diasMora` | number
`interesMora` | number
`montoPagado` | number

## Example

```typescript
import type { CuotaResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "numeroCuota": null,
  "fechaVencimiento": null,
  "fechaPago": null,
  "capital": null,
  "interes": null,
  "montoCuota": null,
  "saldoInsoluto": null,
  "estado": null,
  "diasMora": null,
  "interesMora": null,
  "montoPagado": null,
} satisfies CuotaResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as CuotaResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


