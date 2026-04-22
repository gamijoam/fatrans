
# UsuarioDTO

Información del usuario autenticado

## Properties

Name | Type
------------ | -------------
`id` | string
`nombreUsuario` | string
`correoElectronico` | string
`nombreCompleto` | string
`rol` | string

## Example

```typescript
import type { UsuarioDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "nombreUsuario": null,
  "correoElectronico": null,
  "nombreCompleto": null,
  "rol": null,
} satisfies UsuarioDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as UsuarioDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


