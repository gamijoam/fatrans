
# TokenValidacionDTO


## Properties

Name | Type
------------ | -------------
`usuarioId` | string
`nombreUsuario` | string
`correoElectronico` | string
`rol` | string
`expiracion` | Date
`valido` | boolean

## Example

```typescript
import type { TokenValidacionDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "usuarioId": null,
  "nombreUsuario": null,
  "correoElectronico": null,
  "rol": null,
  "expiracion": null,
  "valido": null,
} satisfies TokenValidacionDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as TokenValidacionDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


