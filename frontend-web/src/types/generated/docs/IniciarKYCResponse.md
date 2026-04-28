
# IniciarKYCResponse


## Properties

Name | Type
------------ | -------------
`verificacionId` | string
`nivel` | string
`estado` | string
`documentosRequeridos` | Array&lt;string&gt;
`mensaje` | string

## Example

```typescript
import type { IniciarKYCResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "verificacionId": null,
  "nivel": null,
  "estado": null,
  "documentosRequeridos": null,
  "mensaje": null,
} satisfies IniciarKYCResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as IniciarKYCResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


