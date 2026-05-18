
# Direccion


## Properties

Name | Type
------------ | -------------
`calle` | string
`numero` | string
`ciudad` | string
`departamento` | string
`codigoPostal` | string
`pais` | string

## Example

```typescript
import type { Direccion } from ''

// TODO: Update the object below with actual values
const example = {
  "calle": null,
  "numero": null,
  "ciudad": null,
  "departamento": null,
  "codigoPostal": null,
  "pais": null,
} satisfies Direccion

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as Direccion
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


