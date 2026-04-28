
# RefreshTokenRequestDTO

Solicitud de refresh de token

## Properties

Name | Type
------------ | -------------
`refreshToken` | string

## Example

```typescript
import type { RefreshTokenRequestDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "refreshToken": eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...,
} satisfies RefreshTokenRequestDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as RefreshTokenRequestDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


