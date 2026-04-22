
# SolicitudRegistroRequestDTO


## Properties

Name | Type
------------ | -------------
`nombreCompleto` | string
`cedula` | string
`correoElectronico` | string
`telefono` | string
`empresa` | string

## Example

```typescript
import type { SolicitudRegistroRequestDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "nombreCompleto": null,
  "cedula": null,
  "correoElectronico": null,
  "telefono": null,
  "empresa": null,
} satisfies SolicitudRegistroRequestDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as SolicitudRegistroRequestDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


