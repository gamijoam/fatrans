
# RendimientoResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`cuentaAhorroId` | string
`periodoInicio` | Date
`periodoFin` | Date
`saldoPromedioPeriodo` | number
`tasaAplicada` | number
`montoRendimiento` | number
`tipo` | string
`estadoAplicacion` | string
`fechaCalculo` | Date

## Example

```typescript
import type { RendimientoResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "cuentaAhorroId": null,
  "periodoInicio": null,
  "periodoFin": null,
  "saldoPromedioPeriodo": null,
  "tasaAplicada": null,
  "montoRendimiento": null,
  "tipo": null,
  "estadoAplicacion": null,
  "fechaCalculo": null,
} satisfies RendimientoResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as RendimientoResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


