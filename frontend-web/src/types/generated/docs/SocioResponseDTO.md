
# SocioResponseDTO


## Properties

Name | Type
------------ | -------------
`id` | string
`numeroSocio` | string
`tipoDocumento` | string
`numeroDocumento` | string
`primerNombre` | string
`segundoNombre` | string
`primerApellido` | string
`segundoApellido` | string
`fechaNacimiento` | Date
`genero` | string
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
`salario` | number
`montoAhorro` | number
`numeroCuentaNomina` | string
`bancoNomina` | string
`contactoEmergencia` | [ContactoEmergencia](ContactoEmergencia.md)
`estado` | string
`fechaIngreso` | Date
`fechaRegistro` | Date
`fechaActualizacion` | Date
`fechaActivacion` | Date
`fechaDesactivacion` | Date
`motivoDesactivacion` | string
`roles` | Set&lt;string&gt;

## Example

```typescript
import type { SocioResponseDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "numeroSocio": null,
  "tipoDocumento": null,
  "numeroDocumento": null,
  "primerNombre": null,
  "segundoNombre": null,
  "primerApellido": null,
  "segundoApellido": null,
  "fechaNacimiento": null,
  "genero": null,
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
  "salario": null,
  "montoAhorro": null,
  "numeroCuentaNomina": null,
  "bancoNomina": null,
  "contactoEmergencia": null,
  "estado": null,
  "fechaIngreso": null,
  "fechaRegistro": null,
  "fechaActualizacion": null,
  "fechaActivacion": null,
  "fechaDesactivacion": null,
  "motivoDesactivacion": null,
  "roles": null,
} satisfies SocioResponseDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as SocioResponseDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


