
# RevisionResponse


## Properties

Name | Type
------------ | -------------
`verificacionId` | string
`socioId` | string
`nivel` | string
`estado` | string
`fechaInicio` | Date
`fechaEnvio` | Date
`documentos` | [Array&lt;DocumentoRevisionResponse&gt;](DocumentoRevisionResponse.md)
`consentimiento` | [ConsentimientoResponse](ConsentimientoResponse.md)

## Example

```typescript
import type { RevisionResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "verificacionId": null,
  "socioId": null,
  "nivel": null,
  "estado": null,
  "fechaInicio": null,
  "fechaEnvio": null,
  "documentos": null,
  "consentimiento": null,
} satisfies RevisionResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as RevisionResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


