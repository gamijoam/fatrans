
# CreateBeneficiarioRequestDTO


## Properties

Name | Type
------------ | -------------
`nombreCompleto` | string
`numeroDocumento` | string
`tipoDocumento` | string
`parentesco` | string
`porcentaje` | number
`telefono` | string

## Example

```typescript
import type { CreateBeneficiarioRequestDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "nombreCompleto": null,
  "numeroDocumento": null,
  "tipoDocumento": null,
  "parentesco": null,
  "porcentaje": null,
  "telefono": null,
} satisfies CreateBeneficiarioRequestDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as CreateBeneficiarioRequestDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


