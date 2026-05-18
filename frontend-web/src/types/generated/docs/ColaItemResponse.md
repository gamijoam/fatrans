
# ColaItemResponse


## Properties

Name | Type
------------ | -------------
`verificacionId` | string
`socioId` | string
`nivel` | string
`estado` | string
`fechaEnvio` | Date
`tiempoEnCola` | string
`documentos` | [Array&lt;DocumentoResumenResponse&gt;](DocumentoResumenResponse.md)

## Example

```typescript
import type { ColaItemResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "verificacionId": null,
  "socioId": null,
  "nivel": null,
  "estado": null,
  "fechaEnvio": null,
  "tiempoEnCola": null,
  "documentos": null,
} satisfies ColaItemResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as ColaItemResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


