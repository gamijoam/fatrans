
# SubirDocumentoResponse


## Properties

Name | Type
------------ | -------------
`documentoId` | string
`tipoDocumento` | string
`nombreOriginal` | string
`estado` | string
`mensaje` | string

## Example

```typescript
import type { SubirDocumentoResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "documentoId": null,
  "tipoDocumento": null,
  "nombreOriginal": null,
  "estado": null,
  "mensaje": null,
} satisfies SubirDocumentoResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as SubirDocumentoResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


