
# SubirDocumentoRequest


## Properties

Name | Type
------------ | -------------
`verificacionId` | string
`tipoDocumento` | string
`archivoBase64` | string
`nombreOriginal` | string
`tamanoBytes` | number
`mimeType` | string
`fechaExpiracionDocumento` | Date

## Example

```typescript
import type { SubirDocumentoRequest } from ''

// TODO: Update the object below with actual values
const example = {
  "verificacionId": null,
  "tipoDocumento": null,
  "archivoBase64": null,
  "nombreOriginal": null,
  "tamanoBytes": null,
  "mimeType": null,
  "fechaExpiracionDocumento": null,
} satisfies SubirDocumentoRequest

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as SubirDocumentoRequest
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


