
# LoginRequestDTO

Solicitud de inicio de sesión

## Properties

Name | Type
------------ | -------------
`identificador` | string
`password` | string

## Example

```typescript
import type { LoginRequestDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "identificador": usuario@ejemplo.com,
  "password": ContraseñaSegura123!,
} satisfies LoginRequestDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as LoginRequestDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


