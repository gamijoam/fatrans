
# LoginResponseDTO

Respuesta de autenticación

## Properties

Name | Type
------------ | -------------
`accessToken` | string
`refreshToken` | string
`tokenType` | string
`expiresIn` | number
`usuario` | [UsuarioDTO](UsuarioDTO.md)

## Example

```typescript
import type { LoginResponseDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "accessToken": null,
  "refreshToken": null,
  "tokenType": Bearer,
  "expiresIn": 900,
  "usuario": null,
} satisfies LoginResponseDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as LoginResponseDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


