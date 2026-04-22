
# EstadoActualResponse


## Properties

Name | Type
------------ | -------------
`pendientes` | number
`enRevision` | number
`aprobados` | number
`rechazados` | number
`expirados` | number

## Example

```typescript
import type { EstadoActualResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "pendientes": null,
  "enRevision": null,
  "aprobados": null,
  "rechazados": null,
  "expirados": null,
} satisfies EstadoActualResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as EstadoActualResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


