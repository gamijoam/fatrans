
# RevocarConsentimientoResponse


## Properties

Name | Type
------------ | -------------
`consentimientoId` | string
`mensaje` | string
`fechaRevocacion` | Date
`revocacionExitosa` | boolean

## Example

```typescript
import type { RevocarConsentimientoResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "consentimientoId": null,
  "mensaje": null,
  "fechaRevocacion": null,
  "revocacionExitosa": null,
} satisfies RevocarConsentimientoResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as RevocarConsentimientoResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


