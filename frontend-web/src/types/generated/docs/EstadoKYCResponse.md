
# EstadoKYCResponse


## Properties

Name | Type
------------ | -------------
`verificacionId` | string
`socioId` | string
`nivel` | string
`estado` | string
`descripcionEstado` | string
`fechaInicio` | Date
`fechaExpiracion` | Date
`diasRestantes` | number
`documentosRequeridos` | number
`documentosValidos` | number
`documentos` | [Array&lt;DocumentoEstadoResponse&gt;](DocumentoEstadoResponse.md)
`comentarioRevision` | string
`motivoRechazo` | string

## Example

```typescript
import type { EstadoKYCResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "verificacionId": null,
  "socioId": null,
  "nivel": null,
  "estado": null,
  "descripcionEstado": null,
  "fechaInicio": null,
  "fechaExpiracion": null,
  "diasRestantes": null,
  "documentosRequeridos": null,
  "documentosValidos": null,
  "documentos": null,
  "comentarioRevision": null,
  "motivoRechazo": null,
} satisfies EstadoKYCResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as EstadoKYCResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


