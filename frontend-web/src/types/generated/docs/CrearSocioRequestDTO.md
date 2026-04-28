
# CrearSocioRequestDTO


## Properties

Name | Type
------------ | -------------
`primerNombre` | string
`segundoNombre` | string
`primerApellido` | string
`segundoApellido` | string
`fechaNacimiento` | Date
`genero` | string
`estadoCivil` | string
`tipoDocumento` | string
`numeroDocumento` | string
`correoElectronico` | string
`telefonoPrincipal` | string
`telefonoSecundario` | string
`tipoContrato` | string
`fechaIngreso` | Date
`empresa` | string
`departamento` | string
`cargo` | string
`salario` | number
`direccionResidencia` | [Direccion](Direccion.md)
`direccionLaboral` | [Direccion](Direccion.md)
`contactoEmergencia` | [ContactoEmergencia](ContactoEmergencia.md)
`numeroCuentaNomina` | string
`bancoNomina` | string

## Example

```typescript
import type { CrearSocioRequestDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "primerNombre": null,
  "segundoNombre": null,
  "primerApellido": null,
  "segundoApellido": null,
  "fechaNacimiento": null,
  "genero": null,
  "estadoCivil": null,
  "tipoDocumento": null,
  "numeroDocumento": null,
  "correoElectronico": null,
  "telefonoPrincipal": null,
  "telefonoSecundario": null,
  "tipoContrato": null,
  "fechaIngreso": null,
  "empresa": null,
  "departamento": null,
  "cargo": null,
  "salario": null,
  "direccionResidencia": null,
  "direccionLaboral": null,
  "contactoEmergencia": null,
  "numeroCuentaNomina": null,
  "bancoNomina": null,
} satisfies CrearSocioRequestDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as CrearSocioRequestDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


