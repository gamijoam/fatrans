
# ActualizarSocioDTO


## Properties

Name | Type
------------ | -------------
`primerNombre` | string
`segundoNombre` | string
`primerApellido` | string
`segundoApellido` | string
`tipoDocumento` | string
`numeroDocumento` | string
`genero` | string
`fechaNacimiento` | Date
`estadoCivil` | string
`correoElectronico` | string
`telefonoPrincipal` | string
`telefonoSecundario` | string
`direccionResidencia` | [Direccion](Direccion.md)
`direccionLaboral` | [Direccion](Direccion.md)
`empresa` | string
`departamento` | string
`cargo` | string
`tipoContrato` | string
`numeroCuentaNomina` | string
`bancoNomina` | string
`contactoEmergencia` | [ContactoEmergencia](ContactoEmergencia.md)

## Example

```typescript
import type { ActualizarSocioDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "primerNombre": null,
  "segundoNombre": null,
  "primerApellido": null,
  "segundoApellido": null,
  "tipoDocumento": null,
  "numeroDocumento": null,
  "genero": null,
  "fechaNacimiento": null,
  "estadoCivil": null,
  "correoElectronico": null,
  "telefonoPrincipal": null,
  "telefonoSecundario": null,
  "direccionResidencia": null,
  "direccionLaboral": null,
  "empresa": null,
  "departamento": null,
  "cargo": null,
  "tipoContrato": null,
  "numeroCuentaNomina": null,
  "bancoNomina": null,
  "contactoEmergencia": null,
} satisfies ActualizarSocioDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as ActualizarSocioDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


