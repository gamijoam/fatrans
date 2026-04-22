
# MovimientosListResponse


## Properties

Name | Type
------------ | -------------
`numeroCuenta` | string
`pagina` | number
`tamanio` | number
`totalElementos` | number
`totalPaginas` | number
`movimientos` | [Array&lt;MovimientoResponse&gt;](MovimientoResponse.md)

## Example

```typescript
import type { MovimientosListResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "numeroCuenta": null,
  "pagina": null,
  "tamanio": null,
  "totalElementos": null,
  "totalPaginas": null,
  "movimientos": null,
} satisfies MovimientosListResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as MovimientosListResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


