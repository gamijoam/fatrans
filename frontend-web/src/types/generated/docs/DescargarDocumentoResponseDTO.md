
# DescargarDocumentoResponseDTO


## Properties

Name | Type
------------ | -------------
`documentoId` | string
`preSignedUrl` | string
`urlExpiraEn` | number
`fechaExpiracion` | Date

## Example

```typescript
import type { DescargarDocumentoResponseDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "documentoId": null,
  "preSignedUrl": null,
  "urlExpiraEn": null,
  "fechaExpiracion": null,
} satisfies DescargarDocumentoResponseDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as DescargarDocumentoResponseDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


