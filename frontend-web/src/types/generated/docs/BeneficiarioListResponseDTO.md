
# BeneficiarioListResponseDTO


## Properties

Name | Type
------------ | -------------
`beneficiarios` | [Array&lt;BeneficiarioResponseDTO&gt;](BeneficiarioResponseDTO.md)
`total` | number
`sumaPorcentajes` | number

## Example

```typescript
import type { BeneficiarioListResponseDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "beneficiarios": null,
  "total": null,
  "sumaPorcentajes": null,
} satisfies BeneficiarioListResponseDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as BeneficiarioListResponseDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


