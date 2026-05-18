# KYCApi

All URIs are relative to *http://localhost:18080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**consultarEstado**](KYCApi.md#consultarestado) | **GET** /api/v1/kyc/estado | Consultar estado del KYC |
| [**eliminarDocumento**](KYCApi.md#eliminardocumento) | **DELETE** /api/v1/kyc/documentos/{documentoId} | Eliminar documento |
| [**enviarDocumentos**](KYCApi.md#enviardocumentosoperation) | **POST** /api/v1/kyc/enviar | Enviar documentos para revision |
| [**iniciarKYC**](KYCApi.md#iniciarkycoperation) | **POST** /api/v1/kyc/iniciar | Iniciar proceso KYC |
| [**revocarConsentimiento**](KYCApi.md#revocarconsentimientooperation) | **POST** /api/v1/kyc/revocar-consentimiento | Revocar consentimiento para tratamiento de datos |
| [**subirDocumento**](KYCApi.md#subirdocumentooperation) | **POST** /api/v1/kyc/documentos | Subir documento de identidad |



## consultarEstado

> EstadoKYCResponse consultarEstado()

Consultar estado del KYC

### Example

```ts
import {
  Configuration,
  KYCApi,
} from '';
import type { ConsultarEstadoRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new KYCApi();

  try {
    const data = await api.consultarEstado();
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**EstadoKYCResponse**](EstadoKYCResponse.md)

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


## eliminarDocumento

> EliminarDocumentoResponse eliminarDocumento(documentoId)

Eliminar documento

### Example

```ts
import {
  Configuration,
  KYCApi,
} from '';
import type { EliminarDocumentoRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new KYCApi();

  const body = {
    // string
    documentoId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies EliminarDocumentoRequest;

  try {
    const data = await api.eliminarDocumento(body);
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

[**EliminarDocumentoResponse**](EliminarDocumentoResponse.md)

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


## enviarDocumentos

> EnviarDocumentosResponse enviarDocumentos(enviarDocumentosRequest)

Enviar documentos para revision

### Example

```ts
import {
  Configuration,
  KYCApi,
} from '';
import type { EnviarDocumentosOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new KYCApi();

  const body = {
    // EnviarDocumentosRequest
    enviarDocumentosRequest: ...,
  } satisfies EnviarDocumentosOperationRequest;

  try {
    const data = await api.enviarDocumentos(body);
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
| **enviarDocumentosRequest** | [EnviarDocumentosRequest](EnviarDocumentosRequest.md) |  | |

### Return type

[**EnviarDocumentosResponse**](EnviarDocumentosResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## iniciarKYC

> IniciarKYCResponse iniciarKYC(iniciarKYCRequest)

Iniciar proceso KYC

### Example

```ts
import {
  Configuration,
  KYCApi,
} from '';
import type { IniciarKYCOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new KYCApi();

  const body = {
    // IniciarKYCRequest
    iniciarKYCRequest: ...,
  } satisfies IniciarKYCOperationRequest;

  try {
    const data = await api.iniciarKYC(body);
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
| **iniciarKYCRequest** | [IniciarKYCRequest](IniciarKYCRequest.md) |  | |

### Return type

[**IniciarKYCResponse**](IniciarKYCResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## revocarConsentimiento

> RevocarConsentimientoResponse revocarConsentimiento(revocarConsentimientoRequest)

Revocar consentimiento para tratamiento de datos

### Example

```ts
import {
  Configuration,
  KYCApi,
} from '';
import type { RevocarConsentimientoOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new KYCApi();

  const body = {
    // RevocarConsentimientoRequest
    revocarConsentimientoRequest: ...,
  } satisfies RevocarConsentimientoOperationRequest;

  try {
    const data = await api.revocarConsentimiento(body);
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
| **revocarConsentimientoRequest** | [RevocarConsentimientoRequest](RevocarConsentimientoRequest.md) |  | |

### Return type

[**RevocarConsentimientoResponse**](RevocarConsentimientoResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## subirDocumento

> SubirDocumentoResponse subirDocumento(subirDocumentoRequest)

Subir documento de identidad

### Example

```ts
import {
  Configuration,
  KYCApi,
} from '';
import type { SubirDocumentoOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new KYCApi();

  const body = {
    // SubirDocumentoRequest
    subirDocumentoRequest: ...,
  } satisfies SubirDocumentoOperationRequest;

  try {
    const data = await api.subirDocumento(body);
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
| **subirDocumentoRequest** | [SubirDocumentoRequest](SubirDocumentoRequest.md) |  | |

### Return type

[**SubirDocumentoResponse**](SubirDocumentoResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

