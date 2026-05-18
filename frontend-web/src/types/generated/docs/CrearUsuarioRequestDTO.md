
# CrearUsuarioRequestDTO

Solicitud para crear un usuario vinculado a un socio

## Properties

Name | Type
------------ | -------------
`socioId` | string
`nombreUsuario` | string
`password` | string

## Example

```typescript
import type { CrearUsuarioRequestDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "socioId": 550e8400-e29b-41d4-a716-446655440000,
  "nombreUsuario": juan.perez,
  "password": TempPassword123!,
} satisfies CrearUsuarioRequestDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as CrearUsuarioRequestDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


