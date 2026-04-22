
# PagoCuotaResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`numeroCuota` | number
`estado` | string
`montoPagado` | number
`fechaPago` | Date
`referenciaPago` | string
`saldoInsolutoRestante` | number
`mensaje` | string

## Example

```typescript
import type { PagoCuotaResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "numeroCuota": null,
  "estado": null,
  "montoPagado": null,
  "fechaPago": null,
  "referenciaPago": null,
  "saldoInsolutoRestante": null,
  "mensaje": null,
} satisfies PagoCuotaResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as PagoCuotaResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


