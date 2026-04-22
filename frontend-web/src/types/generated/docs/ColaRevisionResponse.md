
# ColaRevisionResponse


## Properties

Name | Type
------------ | -------------
`pagina` | number
`tamanio` | number
`totalElementos` | number
`totalPaginas` | number
`cola` | [Array&lt;ColaItemResponse&gt;](ColaItemResponse.md)

## Example

```typescript
import type { ColaRevisionResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "pagina": null,
  "tamanio": null,
  "totalElementos": null,
  "totalPaginas": null,
  "cola": null,
} satisfies ColaRevisionResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as ColaRevisionResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


