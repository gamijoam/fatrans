
# TipoCreditoPublicResponse


## Properties

Name | Type
------------ | -------------
`id` | number
`codigo` | string
`nombre` | string
`descripcion` | string
`tasaInteresAnual` | number
`plazoMinimoMeses` | number
`plazoMaximoMeses` | number
`montoMinimo` | number
`montoMaximo` | number
`porcentajeRequerimientoColateral` | number
`diasGracia` | number

## Example

```typescript
import type { TipoCreditoPublicResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "codigo": null,
  "nombre": null,
  "descripcion": null,
  "tasaInteresAnual": null,
  "plazoMinimoMeses": null,
  "plazoMaximoMeses": null,
  "montoMinimo": null,
  "montoMaximo": null,
  "porcentajeRequerimientoColateral": null,
  "diasGracia": null,
} satisfies TipoCreditoPublicResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as TipoCreditoPublicResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


