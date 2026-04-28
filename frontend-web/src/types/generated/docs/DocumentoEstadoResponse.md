
# DocumentoEstadoResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`tipo` | string
`descripcion` | string
`estado` | string
`nombreOriginal` | string
`fechaSubida` | Date
`motivoRechazo` | string

## Example

```typescript
import type { DocumentoEstadoResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "tipo": null,
  "descripcion": null,
  "estado": null,
  "nombreOriginal": null,
  "fechaSubida": null,
  "motivoRechazo": null,
} satisfies DocumentoEstadoResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as DocumentoEstadoResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


