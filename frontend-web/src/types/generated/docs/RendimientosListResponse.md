
# RendimientosListResponse


## Properties

Name | Type
------------ | -------------
`numeroCuenta` | string
`pagina` | number
`tamanio` | number
`totalElementos` | number
`totalPaginas` | number
`rendimientos` | [Array&lt;RendimientoResponse&gt;](RendimientoResponse.md)

## Example

```typescript
import type { RendimientosListResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "numeroCuenta": null,
  "pagina": null,
  "tamanio": null,
  "totalElementos": null,
  "totalPaginas": null,
  "rendimientos": null,
} satisfies RendimientosListResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as RendimientosListResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


