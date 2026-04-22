
# DeleteBeneficiarioResponseDTO


## Properties

Name | Type
------------ | -------------
`id` | string
`socioId` | string
`activo` | boolean
`mensaje` | string
`sumaPorcentajesRestantes` | number
`warning` | string

## Example

```typescript
import type { DeleteBeneficiarioResponseDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "socioId": null,
  "activo": null,
  "mensaje": null,
  "sumaPorcentajesRestantes": null,
  "warning": null,
} satisfies DeleteBeneficiarioResponseDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as DeleteBeneficiarioResponseDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


