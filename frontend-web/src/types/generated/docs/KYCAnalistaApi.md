# KYCAnalistaApi

All URIs are relative to *http://localhost:18080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**aprobarVerificacion**](KYCAnalistaApi.md#aprobarverificacionoperation) | **POST** /api/v1/kyc/revision/{verificacionId}/aprobar | Aprobar verificacion KYC |
| [**obtenerDetalleRevision**](KYCAnalistaApi.md#obtenerdetallerevision) | **GET** /api/v1/kyc/revision/{verificacionId} | Obtener detalle de verificacion para revision |
| [**rechazarVerificacion**](KYCAnalistaApi.md#rechazarverificacionoperation) | **POST** /api/v1/kyc/revision/{verificacionId}/rechazar | Rechazar verificacion KYC |
| [**solicitarInfo**](KYCAnalistaApi.md#solicitarinfooperation) | **POST** /api/v1/kyc/revision/{verificacionId}/solicitar-info | Solicitar informacion adicional |



## aprobarVerificacion

> RevisionDecisionResponse aprobarVerificacion(verificacionId, aprobarVerificacionRequest)

Aprobar verificacion KYC

### Example

```ts
import {
  Configuration,
  KYCAnalistaApi,
} from '';
import type { AprobarVerificacionOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new KYCAnalistaApi();

  const body = {
    // string
    verificacionId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // AprobarVerificacionRequest (optional)
    aprobarVerificacionRequest: ...,
  } satisfies AprobarVerificacionOperationRequest;

  try {
    const data = await api.aprobarVerificacion(body);
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
| **verificacionId** | `string` |  | [Defaults to `undefined`] |
| **aprobarVerificacionRequest** | [AprobarVerificacionRequest](AprobarVerificacionRequest.md) |  | [Optional] |

### Return type

[**RevisionDecisionResponse**](RevisionDecisionResponse.md)

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


## obtenerDetalleRevision

> RevisionResponse obtenerDetalleRevision(verificacionId)

Obtener detalle de verificacion para revision

### Example

```ts
import {
  Configuration,
  KYCAnalistaApi,
} from '';
import type { ObtenerDetalleRevisionRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new KYCAnalistaApi();

  const body = {
    // string
    verificacionId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies ObtenerDetalleRevisionRequest;

  try {
    const data = await api.obtenerDetalleRevision(body);
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
| **verificacionId** | `string` |  | [Defaults to `undefined`] |

### Return type

[**RevisionResponse**](RevisionResponse.md)

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


## rechazarVerificacion

> RevisionDecisionResponse rechazarVerificacion(verificacionId, rechazarVerificacionRequest)

Rechazar verificacion KYC

### Example

```ts
import {
  Configuration,
  KYCAnalistaApi,
} from '';
import type { RechazarVerificacionOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new KYCAnalistaApi();

  const body = {
    // string
    verificacionId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // RechazarVerificacionRequest
    rechazarVerificacionRequest: ...,
  } satisfies RechazarVerificacionOperationRequest;

  try {
    const data = await api.rechazarVerificacion(body);
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
| **verificacionId** | `string` |  | [Defaults to `undefined`] |
| **rechazarVerificacionRequest** | [RechazarVerificacionRequest](RechazarVerificacionRequest.md) |  | |

### Return type

[**RevisionDecisionResponse**](RevisionDecisionResponse.md)

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


## solicitarInfo

> RevisionDecisionResponse solicitarInfo(verificacionId, solicitarInfoRequest)

Solicitar informacion adicional

### Example

```ts
import {
  Configuration,
  KYCAnalistaApi,
} from '';
import type { SolicitarInfoOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new KYCAnalistaApi();

  const body = {
    // string
    verificacionId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // SolicitarInfoRequest
    solicitarInfoRequest: ...,
  } satisfies SolicitarInfoOperationRequest;

  try {
    const data = await api.solicitarInfo(body);
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
| **verificacionId** | `string` |  | [Defaults to `undefined`] |
| **solicitarInfoRequest** | [SolicitarInfoRequest](SolicitarInfoRequest.md) |  | |

### Return type

[**RevisionDecisionResponse**](RevisionDecisionResponse.md)

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

