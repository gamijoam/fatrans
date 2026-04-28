
# MovimientoResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`numeroOperacion` | string
`cuentaAhorroId` | string
`socioId` | string
`tipo` | string
`monto` | number
`saldoAnterior` | number
`saldoPosterior` | number
`descripcion` | string
`referencia` | string
`canalOrigen` | string
`estado` | string
`fechaMovimiento` | Date
`fechaValor` | Date

## Example

```typescript
import type { MovimientoResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "numeroOperacion": null,
  "cuentaAhorroId": null,
  "socioId": null,
  "tipo": null,
  "monto": null,
  "saldoAnterior": null,
  "saldoPosterior": null,
  "descripcion": null,
  "referencia": null,
  "canalOrigen": null,
  "estado": null,
  "fechaMovimiento": null,
  "fechaValor": null,
} satisfies MovimientoResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as MovimientoResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


