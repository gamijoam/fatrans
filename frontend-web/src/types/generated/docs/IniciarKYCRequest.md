
# IniciarKYCRequest


## Properties

Name | Type
------------ | -------------
`nivel` | string
`consentimientoAceptado` | boolean
`versionPolitica` | string
`ipCliente` | string
`userAgent` | string

## Example

```typescript
import type { IniciarKYCRequest } from ''

// TODO: Update the object below with actual values
const example = {
  "nivel": null,
  "consentimientoAceptado": null,
  "versionPolitica": null,
  "ipCliente": null,
  "userAgent": null,
} satisfies IniciarKYCRequest

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as IniciarKYCRequest
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


