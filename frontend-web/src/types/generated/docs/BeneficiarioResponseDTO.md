
# BeneficiarioResponseDTO


## Properties

Name | Type
------------ | -------------
`id` | string
`socioId` | string
`nombreCompleto` | string
`numeroDocumento` | string
`tipoDocumento` | string
`parentesco` | string
`porcentaje` | number
`telefono` | string
`activo` | boolean
`fechaRegistro` | Date
`fechaActualizacion` | Date

## Example

```typescript
import type { BeneficiarioResponseDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "socioId": null,
  "nombreCompleto": null,
  "numeroDocumento": null,
  "tipoDocumento": null,
  "parentesco": null,
  "porcentaje": null,
  "telefono": null,
  "activo": null,
  "fechaRegistro": null,
  "fechaActualizacion": null,
} satisfies BeneficiarioResponseDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as BeneficiarioResponseDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


