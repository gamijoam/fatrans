
# EnviarDocumentosResponse


## Properties

Name | Type
------------ | -------------
`verificacionId` | string
`estado` | string
`documentosEnviados` | number
`mensaje` | string

## Example

```typescript
import type { EnviarDocumentosResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "verificacionId": null,
  "estado": null,
  "documentosEnviados": null,
  "mensaje": null,
} satisfies EnviarDocumentosResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as EnviarDocumentosResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


