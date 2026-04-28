
# DocumentoResponseDTO


## Properties

Name | Type
------------ | -------------
`documentoId` | string
`socioId` | string
`tipo` | string
`nombreArchivo` | string
`estado` | string
`tamanoBytes` | number
`hashArchivo` | string
`clasificacion` | string
`firmaDigital` | string
`preSignedUrl` | string
`urlExpiraEn` | number
`fechaGeneracion` | Date
`fechaExpiracion` | Date

## Example

```typescript
import type { DocumentoResponseDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "documentoId": null,
  "socioId": null,
  "tipo": null,
  "nombreArchivo": null,
  "estado": null,
  "tamanoBytes": null,
  "hashArchivo": null,
  "clasificacion": null,
  "firmaDigital": null,
  "preSignedUrl": null,
  "urlExpiraEn": null,
  "fechaGeneracion": null,
  "fechaExpiracion": null,
} satisfies DocumentoResponseDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as DocumentoResponseDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


