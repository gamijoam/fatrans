
# HistorialItemResponse


## Properties

Name | Type
------------ | -------------
`verificacionId` | string
`nivel` | string
`estado` | string
`fechaInicio` | Date
`fechaCompletado` | Date
`fechaExpiracion` | Date
`diasRestantes` | number
`revisadoPor` | string
`motivoRechazo` | string

## Example

```typescript
import type { HistorialItemResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "verificacionId": null,
  "nivel": null,
  "estado": null,
  "fechaInicio": null,
  "fechaCompletado": null,
  "fechaExpiracion": null,
  "diasRestantes": null,
  "revisadoPor": null,
  "motivoRechazo": null,
} satisfies HistorialItemResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as HistorialItemResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


