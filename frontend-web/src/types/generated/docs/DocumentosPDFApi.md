# DocumentosPDFApi

All URIs are relative to *http://localhost:18080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**descargarDocumento**](DocumentosPDFApi.md#descargardocumento) | **GET** /api/v1/documentos/{documentoId}/descargar | Descargar documento (pre-signed URL) |
| [**generarCartaBeneficiarios**](DocumentosPDFApi.md#generarcartabeneficiarios) | **GET** /api/v1/documentos/carta-beneficiarios/{socioId} | Generar carta de beneficiarios |
| [**generarConstanciaAfiliacion**](DocumentosPDFApi.md#generarconstanciaafiliacion) | **GET** /api/v1/documentos/constancia-afiliacion/{socioId} | Generar constancia de afiliación |
| [**generarContrato**](DocumentosPDFApi.md#generarcontrato) | **GET** /api/v1/documentos/contrato/{solicitudId} | Generar contrato de adhesión (firma digital) |
| [**generarEstadoCuenta**](DocumentosPDFApi.md#generarestadocuenta) | **GET** /api/v1/documentos/estado-cuenta/{cuentaId} | Generar estado de cuenta |
| [**generarPagare**](DocumentosPDFApi.md#generarpagare) | **GET** /api/v1/documentos/pagare/{creditoId} | Generar pagaré (firma digital) |
| [**generarTablaAmortizacion**](DocumentosPDFApi.md#generartablaamortizacion) | **GET** /api/v1/documentos/tabla-amortizacion/{creditoId} | Generar tabla de amortización |
| [**listarDocumentos**](DocumentosPDFApi.md#listardocumentos) | **GET** /api/v1/documentos/socio/{socioId} | Listar documentos de socio |
| [**obtenerDocumento**](DocumentosPDFApi.md#obtenerdocumento) | **GET** /api/v1/documentos/{documentoId} | Obtener metadata de documento |



## descargarDocumento

> DescargarDocumentoResponseDTO descargarDocumento(documentoId)

Descargar documento (pre-signed URL)

### Example

```ts
import {
  Configuration,
  DocumentosPDFApi,
} from '';
import type { DescargarDocumentoRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DocumentosPDFApi();

  const body = {
    // string
    documentoId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies DescargarDocumentoRequest;

  try {
    const data = await api.descargarDocumento(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **documentoId** | `string` |  | [Defaults to `undefined`] |

### Return type

[**DescargarDocumentoResponseDTO**](DescargarDocumentoResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## generarCartaBeneficiarios

> DocumentoResponseDTO generarCartaBeneficiarios(socioId)

Generar carta de beneficiarios

### Example

```ts
import {
  Configuration,
  DocumentosPDFApi,
} from '';
import type { GenerarCartaBeneficiariosRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DocumentosPDFApi();

  const body = {
    // string
    socioId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies GenerarCartaBeneficiariosRequest;

  try {
    const data = await api.generarCartaBeneficiarios(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **socioId** | `string` |  | [Defaults to `undefined`] |

### Return type

[**DocumentoResponseDTO**](DocumentoResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## generarConstanciaAfiliacion

> DocumentoResponseDTO generarConstanciaAfiliacion(socioId)

Generar constancia de afiliación

### Example

```ts
import {
  Configuration,
  DocumentosPDFApi,
} from '';
import type { GenerarConstanciaAfiliacionRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DocumentosPDFApi();

  const body = {
    // string
    socioId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies GenerarConstanciaAfiliacionRequest;

  try {
    const data = await api.generarConstanciaAfiliacion(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **socioId** | `string` |  | [Defaults to `undefined`] |

### Return type

[**DocumentoResponseDTO**](DocumentoResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## generarContrato

> DocumentoResponseDTO generarContrato(solicitudId)

Generar contrato de adhesión (firma digital)

### Example

```ts
import {
  Configuration,
  DocumentosPDFApi,
} from '';
import type { GenerarContratoRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DocumentosPDFApi();

  const body = {
    // string
    solicitudId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies GenerarContratoRequest;

  try {
    const data = await api.generarContrato(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **solicitudId** | `string` |  | [Defaults to `undefined`] |

### Return type

[**DocumentoResponseDTO**](DocumentoResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## generarEstadoCuenta

> DocumentoResponseDTO generarEstadoCuenta(cuentaId)

Generar estado de cuenta

### Example

```ts
import {
  Configuration,
  DocumentosPDFApi,
} from '';
import type { GenerarEstadoCuentaRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DocumentosPDFApi();

  const body = {
    // string
    cuentaId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies GenerarEstadoCuentaRequest;

  try {
    const data = await api.generarEstadoCuenta(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **cuentaId** | `string` |  | [Defaults to `undefined`] |

### Return type

[**DocumentoResponseDTO**](DocumentoResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## generarPagare

> DocumentoResponseDTO generarPagare(creditoId)

Generar pagaré (firma digital)

### Example

```ts
import {
  Configuration,
  DocumentosPDFApi,
} from '';
import type { GenerarPagareRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DocumentosPDFApi();

  const body = {
    // string
    creditoId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies GenerarPagareRequest;

  try {
    const data = await api.generarPagare(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **creditoId** | `string` |  | [Defaults to `undefined`] |

### Return type

[**DocumentoResponseDTO**](DocumentoResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## generarTablaAmortizacion

> DocumentoResponseDTO generarTablaAmortizacion(creditoId)

Generar tabla de amortización

### Example

```ts
import {
  Configuration,
  DocumentosPDFApi,
} from '';
import type { GenerarTablaAmortizacionRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DocumentosPDFApi();

  const body = {
    // string
    creditoId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies GenerarTablaAmortizacionRequest;

  try {
    const data = await api.generarTablaAmortizacion(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **creditoId** | `string` |  | [Defaults to `undefined`] |

### Return type

[**DocumentoResponseDTO**](DocumentoResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## listarDocumentos

> { [key: string]: object; } listarDocumentos(socioId, tipo, estado, page, size)

Listar documentos de socio

### Example

```ts
import {
  Configuration,
  DocumentosPDFApi,
} from '';
import type { ListarDocumentosRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DocumentosPDFApi();

  const body = {
    // string
    socioId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // 'ESTADO_CUENTA' | 'CONSTANCIA_AFILIACION' | 'CONTRATO_ADHESION' | 'PAGARE' | 'TABLA_AMORTIZACION' | 'CARTA_BENEFICIARIOS' (optional)
    tipo: tipo_example,
    // 'GENERADO' | 'ALMACENADO' | 'EXPIRADO' | 'REVOCADO' (optional)
    estado: estado_example,
    // number (optional)
    page: 56,
    // number (optional)
    size: 56,
  } satisfies ListarDocumentosRequest;

  try {
    const data = await api.listarDocumentos(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **socioId** | `string` |  | [Defaults to `undefined`] |
| **tipo** | `ESTADO_CUENTA`, `CONSTANCIA_AFILIACION`, `CONTRATO_ADHESION`, `PAGARE`, `TABLA_AMORTIZACION`, `CARTA_BENEFICIARIOS` |  | [Optional] [Defaults to `undefined`] [Enum: ESTADO_CUENTA, CONSTANCIA_AFILIACION, CONTRATO_ADHESION, PAGARE, TABLA_AMORTIZACION, CARTA_BENEFICIARIOS] |
| **estado** | `GENERADO`, `ALMACENADO`, `EXPIRADO`, `REVOCADO` |  | [Optional] [Defaults to `undefined`] [Enum: GENERADO, ALMACENADO, EXPIRADO, REVOCADO] |
| **page** | `number` |  | [Optional] [Defaults to `0`] |
| **size** | `number` |  | [Optional] [Defaults to `20`] |

### Return type

**{ [key: string]: object; }**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## obtenerDocumento

> DocumentoResponseDTO obtenerDocumento(documentoId)

Obtener metadata de documento

### Example

```ts
import {
  Configuration,
  DocumentosPDFApi,
} from '';
import type { ObtenerDocumentoRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DocumentosPDFApi();

  const body = {
    // string
    documentoId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies ObtenerDocumentoRequest;

  try {
    const data = await api.obtenerDocumento(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **documentoId** | `string` |  | [Defaults to `undefined`] |

### Return type

[**DocumentoResponseDTO**](DocumentoResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

